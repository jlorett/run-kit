package com.joshualorett.fusedapp.session

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.joshualorett.fusedapp.database.RoomSessionDaoDelegate
import com.joshualorett.fusedapp.database.active.RoomActiveSessionDaoDelegate
import com.joshualorett.fusedapp.location.FusedLocationTracker
import com.joshualorett.fusedapp.location.LocationTracker
import com.joshualorett.fusedapp.notification.SessionNotificationDelegate
import com.joshualorett.fusedapp.time.ElapsedTimeTracker
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
    private lateinit var activeSessionRepository: ActiveSessionRepository
    private val binder: IBinder = FusedLocationUpdateServiceBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var serviceHandler: Handler
    private lateinit var locationTracker: LocationTracker
    private lateinit var notificationDelegate: SessionNotificationDelegate
    private var notifySessionJob: Job? = null
    private var changingConfiguration = false
    private var unbound = false

    override fun onCreate() {
        super.onCreate()
        val handlerThread = HandlerThread(tag)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        locationTracker = FusedLocationTracker(LocationServices.getFusedLocationProviderClient(applicationContext),
            serviceHandler.looper)
        activeSessionRepository = FusedActiveSessionRepository(RoomSessionDaoDelegate,
            RoomActiveSessionDaoDelegate,
            ElapsedTimeTracker(),
            locationTracker)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationDelegate = SessionNotificationDelegate(this, notificationManager,
            notificationId, channelId, extraToggleSessionAction, FusedSessionService::class.java)
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
                when (session().first().state) {
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
        changingConfiguration = false
        unbound = false
        notifySessionJob?.cancel()
        stopForeground(true)
        return binder
    }

    /***
     * When a client rebinds to the service, stop being in the foreground and clear out the
     * notification.
     */
    override fun onRebind(intent: Intent?) {
        Log.i(tag, "in onRebind()")
        changingConfiguration = false
        unbound = false
        notifySessionJob?.cancel()
        stopForeground(true)
        super.onRebind(intent)
    }

    /***
     * When the last client unbinds from this service, check and display a notification.
     * Only display the notification if [onUnbind] has really gone away and not unbound as part of
     * an orientation change.
     */
    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "in onUnbind()")
        unbound = true
        val configurationChanged = changingConfiguration
        lifecycleScope.launch {
            val currentSession = session().first()
            val requestingUpdates = currentSession.state == Session.State.STARTED
            if (!configurationChanged && requestingUpdates) {
                startForeground(notificationId, notificationDelegate.getNotification(currentSession))
                notifySessionJob = notifySession()
            }
        }
        return true
    }

    override fun onDestroy() {
        serviceHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun session(): Flow<Session> {
        return activeSessionRepository.session
            .distinctUntilChanged()
    }

    override fun start() {
        Log.i(tag, "Requesting location updates")
        startService(Intent(applicationContext, FusedSessionService::class.java))
        try {
            lifecycleScope.launch {
                activeSessionRepository.start(this)
            }
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not request updates. $exception")
        }
    }

    override fun stop() {
        Log.i(tag, "Removing location updates")
        try {
            lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    activeSessionRepository.stop()
                }
            }
            stopSelf()
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        }
    }

    override fun pause() {
        Log.i(tag, "Pausing location updates")
        try {
            lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    activeSessionRepository.pause()
                }
            }
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        }
    }

    private fun notifySession(): Job = lifecycleScope.launch {
        activeSessionRepository.session.collect { latestSession ->
            notificationDelegate.notify(latestSession)
        }
    }

    /***
     * Check if our session is still valid after rebinding to the service. This will update the
     * session state if for example, we've lost permission during a session.
     */
    private fun checkSession(hasLocationPermission: Boolean) {
        lifecycleScope.launch {
            val inSession = withContext(Dispatchers.Default) {
                session().first().state == Session.State.STARTED
            }
            val trackingLocation = locationTracker.trackingLocation
            //Tracking stopped, restarting location tracking.
            if (inSession && hasLocationPermission && !trackingLocation) {
                start()
            }
            //Permission lost, pause session.
            if (!hasLocationPermission) {
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
