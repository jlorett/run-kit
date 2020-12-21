package com.joshualorett.fusedapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.joshualorett.fusedapp.location.FusedLocationTracker
import com.joshualorett.fusedapp.location.LocationTracker
import com.joshualorett.fusedapp.notification.SessionNotificationBuilder
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionDao
import com.joshualorett.fusedapp.session.SessionDataStore
import com.joshualorett.fusedapp.session.SessionService
import com.joshualorett.fusedapp.session.time.SessionTimeTracker
import com.joshualorett.fusedapp.session.time.TimeTracker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

@ExperimentalCoroutinesApi
class FusedSessionService : SessionService, LifecycleService() {
    private val pkgName = "com.joshualorett.fusedapp.locationupdatesservice"
    private val extraToggleSession = "$pkgName.toggleSession"
    private val notificationId = 12345678
    private val channelId = "channel_fused_location"
    private val tag = FusedSessionService::class.java.simpleName
    private val sessionDao: SessionDao = SessionDataStore
    private val binder: IBinder = FusedLocationUpdateServiceBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var serviceHandler: Handler
    private lateinit var locationTracker: LocationTracker
    private lateinit var timeTracker: TimeTracker
    private var totalDistance = 0F
    private var trackLocationJob: Job? = null
    private var changingConfiguration = false
    private var unbound = false
    private var lastLocation: Location? = null
    private val _session = MutableStateFlow(Session())
    override val session = _session
    private val _elapsedTime = MutableStateFlow(0L)
    override val elapsedTime: StateFlow<Long> = _elapsedTime

    override fun onCreate() {
        super.onCreate()
        val handlerThread = HandlerThread(tag)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        locationTracker = FusedLocationTracker(LocationServices.getFusedLocationProviderClient(applicationContext),
            serviceHandler.looper)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        lifecycleScope.launch {
            val elapsedTime = withContext(Dispatchers.Default) {
                sessionDao.getSessionFlow().first()
            }.elapsedTime
            timeTracker = SessionTimeTracker(elapsedTime)
            _elapsedTime.value = elapsedTime
            sessionDao.getSessionFlow().collect {
                _session.value = it
                if(unbound) {
                    updateSessionNotification(it)
                }
            }
        }
    }

    /***
     * Posts elapsed time every second.
     */
    private fun startTimeTicker(): Job = lifecycleScope.launch {
        while(true) {
            delay(1000)
            val inSession = session.value.state == Session.State.STARTED
            if(!inSession) {
                cancel()
            }
            _elapsedTime.value = timeTracker.getElapsedTime()
        }
    }

    /***
     *  Stop service if notification stop action was called.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(tag, "Service started")
        val toggleAction = intent?.getBooleanExtra(extraToggleSession, false) ?: false
        if(toggleAction) {
            lifecycleScope.launch {
                val state = _session.value.state
                when (state) {
                    Session.State.STARTED -> pause()
                    Session.State.PAUSED, Session.State.STOPPED -> start()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changingConfiguration = true
    }

    /***
     * When a client binds to the service, stop being in the foreground and clear out the
     * notification.
     */
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.i(tag, "in onBind()")
        stopForeground(true)
        changingConfiguration = false
        unbound = false
        return binder
    }

    /***
     * When a client rebinds to the service, stop being in the foreground and clear out the
     * notification.
     */
    override fun onRebind(intent: Intent?) {
        Log.i(tag, "in onRebind()")
        stopForeground(true)
        changingConfiguration = false
        unbound = false
        super.onRebind(intent)
    }
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */

    /***
     * When the last client unbinds from this service, check and display a notification.
     * Only display the notification if [onUnbind] has really gone away and not unbound as part of
     * an orientation change.
     */
    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "in onUnbind()")
        lifecycleScope.launch {
            val requestingUpdates = _session.value.state == Session.State.STARTED
            if (!changingConfiguration && requestingUpdates) {
                Log.i(tag, "Starting foreground service from Unbind")
                startForeground(notificationId, getNotification(_session.value))
            }
        }
        unbound = true
        return true
    }

    override fun onDestroy() {
        serviceHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun start() {
        Log.i(tag, "Requesting location updates")
        startService(Intent(applicationContext, FusedSessionService::class.java))
        try {
            lifecycleScope.launch {
                timeTracker.start()
                withContext(Dispatchers.Default) {
                    updateSession(timeTracker.getElapsedTime(), totalDistance, Session.State.STARTED)
                }
                startTimeTicker()
                trackLocationJob = trackLocation()
            }
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not request updates. $exception")
            stop()
        }
    }

    override fun stop() {
        Log.i(tag, "Removing location updates")
        try {
            timeTracker.stop()
            updateSession(timeTracker.getElapsedTime(), totalDistance, Session.State.STOPPED)
            timeTracker.reset()
            _elapsedTime.value = 0
            lastLocation = null
            totalDistance = 0F
            stopSelf()
            trackLocationJob?.cancel()
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        }
    }

    override fun pause() {
        Log.i(tag, "Pausing location updates")
        try {
            timeTracker.stop()
            lastLocation = null
            updateSession(timeTracker.getElapsedTime(), totalDistance, Session.State.PAUSED)
            trackLocationJob?.cancel()
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        }
    }

    override fun trackingLocation(): Boolean {
        return locationTracker.trackingLocation.value
    }

    override suspend fun inSession(): Boolean {
        return _session.value.state == Session.State.STARTED
    }

    private fun updateSession(time: Long, distance: Float, state: Session.State): Job = lifecycleScope.launch(Dispatchers.Default) {
        sessionDao.setSession(Session(time, distance, state))
    }

    private fun trackLocation(): Job = lifecycleScope.launch(Dispatchers.Default) {
        locationTracker.track()
            .collect { location ->
                onNewLocation(location)
            }
    }

    private fun onNewLocation(location: Location) {
        Log.i(tag, "New location: $location")
        lastLocation?.let {
            totalDistance += location.distanceTo(it)
        }
        lastLocation = location
        val state = _session.value.state
        updateSession(timeTracker.getElapsedTime(), totalDistance, state)
    }

    private fun updateSessionNotification(session: Session) {
        notificationManager.notify(notificationId, getNotification(session))
    }

    private fun getNotification(session: Session): Notification {
        val state = session.state
        val title = formatHourMinuteSeconds(session.elapsedTime)
        val formattedDistance = formatDistance(session.distance)
        val text = if (state == Session.State.PAUSED) "Paused - $formattedDistance" else formattedDistance
        val contentIntent = Intent(this, MainActivity::class.java)
        val toggleAction = getToggleAction(state)
        return SessionNotificationBuilder
            .toggleAction(toggleAction)
            .build(this, title, text, channelId, contentIntent)
    }

    private fun getToggleAction(state: Session.State): NotificationCompat.Action {
        val toggleActionIntent = Intent(this, FusedSessionService::class.java).also {
            it.putExtra(extraToggleSession, true)
        }
        val toggleActionPendingIntent = PendingIntent.getService(this, 0,
            toggleActionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return when(state) {
            Session.State.STARTED ->
                NotificationCompat.Action(R.drawable.ic_pause_24, getString(R.string.pause),
                    toggleActionPendingIntent)
            Session.State.PAUSED, Session.State.STOPPED ->
                NotificationCompat.Action(R.drawable.ic_play_arrow_24, getString(R.string.resume),
                    toggleActionPendingIntent)
        }
    }

    /**
     * Bind to the [FusedSessionService].
     */
    inner class FusedLocationUpdateServiceBinder : Binder() {
        val service: FusedSessionService
            get() = this@FusedSessionService
    }
}
