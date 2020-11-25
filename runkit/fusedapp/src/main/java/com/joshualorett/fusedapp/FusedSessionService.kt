package com.joshualorett.fusedapp

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
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionDao
import com.joshualorett.fusedapp.session.SessionDataStore
import com.joshualorett.fusedapp.session.SessionService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.DateFormat
import java.util.*

@ExperimentalCoroutinesApi
class FusedSessionService : SessionService, LifecycleService() {
    companion object {
        const val pkgName = "com.joshualorett.fusedapp.locationupdatesservice"
        private const val extraStop = "$pkgName.stop"
        private const val notificationId = 12345678
        private const val channelId = "channel_fused_location"
        private val tag = FusedSessionService::class.java.simpleName
    }
    private val sessionDao: SessionDao = SessionDataStore
    private val binder: IBinder = FusedLocationUpdateServiceBinder()
    private val locations: MutableList<Location> = mutableListOf()
    private lateinit var notificationManager: NotificationManager
    private lateinit var serviceHandler: Handler
    private lateinit var locationTracker: LocationTracker
    private var trackLocationJob: Job? = null
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var changingConfiguration = false
    override val sessionFlow: Flow<Session> = sessionDao.getSessionFlow()

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
    }

    /***
     *  Stop service if notification stop action was called.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(tag, "Service started")
        val stopService = intent?.getBooleanExtra(extraStop, false) ?: false
        if(stopService) {
            stop()
            stopSelf()
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
        super.onRebind(intent)
    }

    /***
     * When the last client unbinds from this service, check and display a notification if we should
     * still are tracking location.
     */
    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "in onUnbind()")
        lifecycleScope.launch {
            val requestingUpdates = sessionFlow.first().state == Session.State.STARTED
            if (!changingConfiguration && requestingUpdates) {
                Log.i(tag, "Starting foreground service from Unbind")
                startForeground(notificationId, getNotification())
            }
        }
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
            updateSession(Session(state = Session.State.STARTED))
            trackLocationJob = lifecycleScope.launch {
                locationTracker.track()
                    .collect { location ->
                        onNewLocation(location)
                    }
            }
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not request updates. $exception")
            updateSession(Session(state = Session.State.STOPPED))
        }
    }

    override fun stop() {
        Log.i(tag, "Removing location updates")
        try {
            trackLocationJob?.cancel()
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        } finally {
            updateSession(Session(state = Session.State.STOPPED))
            locations.clear()
            stopSelf()
        }
    }

    override fun pause() {
        Log.i(tag, "Pausing location updates")
        try {
            trackLocationJob?.cancel()
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        } finally {
            updateSession(Session(state = Session.State.PAUSED))
        }
    }

    override fun trackingLocation(): Boolean {
        return locationTracker.trackingLocation.value
    }

    override suspend fun inSession(): Boolean {
        return sessionFlow.first().state == Session.State.STARTED
    }

    private fun updateSession(session: Session): Job = lifecycleScope.launch(Dispatchers.Default) {
        sessionDao.setSession(session)
    }

    private fun onNewLocation(location: Location) {
        Log.i(tag, "New location: $location")
        val lastLocation = locations.lastOrNull()
        val distance = lastLocation?.distanceTo(location) ?: 0F
        updateSession(Session(distance = distance, state = Session.State.STARTED))
        if(serviceIsRunningInForeground(javaClass, this@FusedSessionService)) {
            notifyNewLocation()
        }
        locations.add(location)
    }

    private fun notifyNewLocation() {
        notificationManager.notify(notificationId, getNotification())
    }

    private fun getNotification(): Notification {
        val title = getString(R.string.location_updated, DateFormat.getDateTimeInstance().format(Date()))
        val text = locationTracker.lastKnownLocation?.getLocationText() ?: "Unknown location"
        val contentIntent = Intent(this, MainActivity::class.java)
        val stopActionIntent = Intent(this, FusedSessionService::class.java).also {
            it.putExtra(extraStop, true)
        }
        return createLocationNotification(this, title, text, channelId, contentIntent, stopActionIntent)
    }

    /**
     * Bind to the [FusedSessionService].
     */
    inner class FusedLocationUpdateServiceBinder : Binder() {
        val service: FusedSessionService
            get() = this@FusedSessionService
    }
}
