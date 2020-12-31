package com.joshualorett.fusedapp.session

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.joshualorett.fusedapp.location.FusedLocationTracker
import com.joshualorett.fusedapp.location.LocationTracker
import com.joshualorett.fusedapp.notification.SessionNotificationDelegate
import com.joshualorett.fusedapp.time.ElapsedTimeTracker
import com.joshualorett.fusedapp.time.TimeTracker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

@ExperimentalCoroutinesApi
class FusedSessionService : SessionService, LifecycleService() {
    private val pkgName = "com.joshualorett.fusedapp.session.fusedsessionservice"
    private val extraToggleSessionAction = "$pkgName.toggleSession"
    private val notificationId = 12345678
    private val channelId = "channel_fused_location"
    private val tag = FusedSessionService::class.java.simpleName
    private val sessionDao: SessionDao = SessionDataStore
    private val binder: IBinder = FusedLocationUpdateServiceBinder()
    private val elapsedTime: Flow<Long> = sessionDao.getElapsedTimeFlow()
    private val session: StateFlow<Session> = combine(
        sessionDao.getSessionStateFlow(),
        sessionDao.getElapsedTimeFlow(),
        sessionDao.getDistanceFlow()) { state: Session.State, time: Long, distance: Float ->
        Session(time, distance, state)
    }.distinctUntilChanged().stateIn(lifecycleScope, SharingStarted.Eagerly, Session())
    private lateinit var notificationManager: NotificationManager
    private lateinit var serviceHandler: Handler
    private lateinit var locationTracker: LocationTracker
    private lateinit var timeTracker: TimeTracker
    private lateinit var notificationDelegate: SessionNotificationDelegate
    private var trackLocationJob: Job? = null
    private var changingConfiguration = false
    private var unbound = false
    private var lastLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        val handlerThread = HandlerThread(tag)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        locationTracker = FusedLocationTracker(LocationServices.getFusedLocationProviderClient(applicationContext),
            serviceHandler.looper)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationDelegate = SessionNotificationDelegate(this, notificationManager,
            notificationId, channelId, extraToggleSessionAction, FusedSessionService::class.java)
        lifecycleScope.launch {
            val elapsedTime = withContext(Dispatchers.Default) {
                elapsedTime.first()
            }
            timeTracker = ElapsedTimeTracker(elapsedTime)
            session.collect { session ->
                if (unbound && !changingConfiguration) {
                    updateSessionNotification(session)
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
            } else {
                sessionDao.setElapsedTime(timeTracker.getElapsedTime())
            }
        }
    }

    /***
     *  Stop service if notification stop action was called.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(tag, "Service started")
        val toggleAction = intent?.getBooleanExtra(extraToggleSessionAction, false) ?: false
        if(toggleAction) {
            lifecycleScope.launch {
                when (session.value.state) {
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


    /***
     * When the last client unbinds from this service, check and display a notification.
     * Only display the notification if [onUnbind] has really gone away and not unbound as part of
     * an orientation change.
     */
    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "in onUnbind()")
        lifecycleScope.launch {
            val requestingUpdates = session.value.state == Session.State.STARTED
            if (!changingConfiguration && requestingUpdates) {
                Log.i(tag, "Starting foreground service from Unbind")
                startForeground(notificationId, notificationDelegate.getNotification(session.value))
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
                    sessionDao.setSessionState(Session.State.STARTED)
                }
                trackLocationJob = trackLocation()
                startTimeTicker()
            }
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not request updates. $exception")
            pause()
        }
    }

    override fun stop() {
        Log.i(tag, "Removing location updates")
        try {
            timeTracker.stop()
            lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    sessionDao.setSessionState(Session.State.STOPPED)
                }
                trackLocationJob?.cancel()
            }
            timeTracker.reset()
            lastLocation = null
            stopSelf()
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        }
    }

    override fun pause() {
        Log.i(tag, "Pausing location updates")
        try {
            timeTracker.stop()
            lastLocation = null
            lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    sessionDao.setSessionState(Session.State.PAUSED)
                }
                trackLocationJob?.cancel()
            }
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        }
    }

    private fun trackLocation(): Job = lifecycleScope.launch(Dispatchers.Default) {
        locationTracker.track()
            .collect { location ->
                onNewLocation(location)
            }
    }

    private suspend fun onNewLocation(location: Location) {
        Log.i(tag, "New location: $location")
        var totalDistance = withContext(Dispatchers.Default) {
            sessionDao.getDistanceFlow().first()
        }
        lastLocation?.let {
            totalDistance += location.distanceTo(it)
        }
        lastLocation = location
        sessionDao.setDistance(totalDistance)
    }

    private fun updateSessionNotification(session: Session) {
        notificationDelegate.notify(session)
    }

    /***
     * Check if our session is still valid after rebinding to the service. This will update the
     * session state if for example, we've lost permission during a session.
     */
    private fun checkSession(hasLocationPermission: Boolean) {
        lifecycleScope.launch {
            val inSession = withContext(Dispatchers.Default) {
                session.first().state == Session.State.STARTED
            }
            val trackingLocation = locationTracker.trackingLocation.value
            //Tracking stopped, restarting location tracking.
            if (inSession && hasLocationPermission && !trackingLocation) {
                start()
            }
            //Permission lost, pause session.
            if (!hasLocationPermission && inSession) {
                pause()
            }
        }
    }

    /**
     * Bind to the [FusedSessionService].
     */
    inner class FusedLocationUpdateServiceBinder : Binder() {
        fun bindService(hasLocationPermission: Boolean): FusedSessionService {
            checkSession(hasLocationPermission)
            return this@FusedSessionService
        }
    }
}
