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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationServices
import com.joshualorett.fusedapp.location.FusedLocationSettings
import com.joshualorett.fusedapp.location.FusedLocationTracker
import com.joshualorett.fusedapp.location.LocationTracker
import com.joshualorett.fusedapp.session.SessionDao
import com.joshualorett.fusedapp.session.SessionDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

@ExperimentalCoroutinesApi
class FusedLocationUpdateService : LifecycleService() {
    companion object {
        const val pkgName = "com.joshualorett.fusedapp.locationupdatesservice"
        const val actionBroadcast: String = "$pkgName.broadcast"
        const val extraLocation: String = "$pkgName.location"
        private const val extraStartedFromNotification = "$pkgName.started_from_notification"
        private const val notificationId = 12345678
        private const val channelId = "channel_fused_location"
        private val tag = FusedLocationUpdateService::class.java.simpleName
    }
    private var trackLocationJob: Job? = null
    private val binder: IBinder = FusedLocationUpdateServiceBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var serviceHandler: Handler
    private lateinit var locationTracker: LocationTracker
    private val sessionDao: SessionDao = SessionDataStore
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var changingConfiguration = false
    val sessionFlow: Flow<Boolean> = sessionDao.getSessionFlow()
    lateinit var trackingLocationFlow: Flow<Boolean>

    override fun onCreate() {
        super.onCreate()
        val handlerThread = HandlerThread(tag)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        locationTracker = FusedLocationTracker(LocationServices.getFusedLocationProviderClient(applicationContext),
            serviceHandler.looper, FusedLocationSettings())
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        trackingLocationFlow = locationTracker.trackingLocation
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(tag, "Service started")
        val startedFromNotification = intent?.getBooleanExtra(extraStartedFromNotification, false) ?: false
        // We got here because the user decided to remove location updates from the notification.
        if(startedFromNotification) {
            stopLocationUpdates()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changingConfiguration = true
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        // Called when a client comes to the foreground and binds with this service. The service
        // should cease to be a foreground service when that happens.
        Log.i(tag, "in onBind()")
        stopForeground(true)
        changingConfiguration = false
        return binder
    }

    override fun onRebind(intent: Intent?) {
        // Called when a client returns to the foreground and binds once again with this service.
        // The service should cease to be a foreground service when that happens.
        Log.i(tag, "in onRebind()")
        stopForeground(true)
        changingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "Last client unbound from service")
        // Called when the last client unbinds from this service. If this method is called due to a
        // configuration change, we do nothing. Otherwise, we make this service a foreground service.
        lifecycleScope.launch {
            val requestingUpdates = sessionDao.getSessionFlow().first()
            if (!changingConfiguration && requestingUpdates) {
                Log.i(tag, "Starting foreground service")
                startForeground(notificationId, getNotification())
            }
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        serviceHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    fun startLocationUpdates() {
        Log.i(tag, "Requesting location updates")
        startService(Intent(applicationContext, FusedLocationUpdateService::class.java))
        try {
            trackLocationJob = lifecycleScope.launch {
                sessionDao.setInSession(true)
                locationTracker.track().collect { location ->
                    onNewLocation(location)
                }
            }
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not request updates. $exception")
            lifecycleScope.launch {
                sessionDao.setInSession(false)
            }
        }
    }

    fun stopLocationUpdates() {
        Log.i(tag, "Removing location updates")
        try {
            trackLocationJob?.cancel()
        } catch (exception: SecurityException) {
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        } finally {
            lifecycleScope.launch {
                sessionDao.setInSession(false)
                stopSelf()
            }
        }
    }

    private fun onNewLocation(location: Location) {
        Log.i(tag, "New location: $location")
        broadcastNewLocation(location)
        if(serviceIsRunningInForeground(this)) {
            notifyNewLocation()
        }
    }

    private fun broadcastNewLocation(location: Location) {
        val locationUpdateIntent = Intent(actionBroadcast)
        locationUpdateIntent.putExtra(extraLocation, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(locationUpdateIntent)
    }

    private fun notifyNewLocation() {
        notificationManager.notify(notificationId, getNotification())
    }

    private fun getNotification(): Notification {
        val title = getString(R.string.location_updated, DateFormat.getDateTimeInstance().format(Date()))
        val text = locationTracker.lastKnownLocation?.getLocationText() ?: "Unknown location"
        val contentIntent = Intent(this, FusedLocationUpdateService::class.java).also {
            it.putExtra(extraStartedFromNotification, true)
        }
        val stopActionIntent = Intent(this, MainActivity::class.java)
        return createLocationNotification(this, title, text, channelId, contentIntent, stopActionIntent)
    }

    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for(service: ActivityManager.RunningServiceInfo in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(javaClass.name == service.service.className) {
                if(service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Bind to the [FusedLocationUpdateService].
     */
    inner class FusedLocationUpdateServiceBinder : Binder() {
        val service: FusedLocationUpdateService
            get() = this@FusedLocationUpdateService
    }
}
