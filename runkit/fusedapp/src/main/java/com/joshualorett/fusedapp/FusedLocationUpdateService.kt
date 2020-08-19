package com.joshualorett.fusedapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import java.text.DateFormat
import java.util.*

class FusedLocationUpdateService : Service() {
    companion object {
        const val pkgName = "com.joshualorett.fusedapp.locationupdatesservice"
        const val actionBroadcast: String = "$pkgName.broadcast"
        const val extraLocation: String = "$pkgName.location"
        private const val extraStartedFromNotification = "$pkgName.started_from_notification"
        private const val notificationId = 12345678
        private const val channelId = "channel_01"
        private val tag = FusedLocationUpdateService::class.java.simpleName
    }
    private val binder: IBinder = LocalBinder()
    private val updateIntervalMs: Long = 10000
    private val fastestUpdaterIntervalMs = updateIntervalMs / 2
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var notificationManager: NotificationManager
    private lateinit var serviceHandler: Handler
    private var location: Location? = null
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var changingConfiguration = false

    override fun onCreate() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let {
                    onNewLocation(it)
                }
            }
        }
        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread(tag)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(tag, "Service started")
        val startedFromNotification = intent?.getBooleanExtra(extraStartedFromNotification, false) ?: false

        // We got here because the user decided to remove location updates from the notification.
        if(startedFromNotification) {
            removeLocationUpdates()
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
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(tag, "in onBind()")
        stopForeground(true)
        changingConfiguration = false
        return binder
    }

    override fun onRebind(intent: Intent?) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(tag, "in onRebind()")
        stopForeground(true)
        changingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "Last client unbound from service")
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!changingConfiguration && LocationUpdatePreferences.requestingLocationUpdates(this)) {
            Log.i(tag, "Starting foreground service")
            startForeground(notificationId, getNotification())
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        serviceHandler.removeCallbacksAndMessages(null)
    }

    fun requestLocationUpdates() {
        Log.i(tag, "Requesting location updates")
        LocationUpdatePreferences.setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, FusedLocationUpdateService::class.java))
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        } catch (exception: SecurityException) {
            LocationUpdatePreferences.setRequestingLocationUpdates(this, false)
            Log.e(tag, "Lost location permission. Could not request updates. $exception")
        }
    }

    fun removeLocationUpdates() {
        Log.i(tag, "Removing location updates")
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            LocationUpdatePreferences.setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (exception: SecurityException) {
            LocationUpdatePreferences.setRequestingLocationUpdates(this, true)
            Log.e(tag, "Lost location permission. Could not remove updates. $exception")
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if(task.isSuccessful && task.result != null) {
                    location = task.result
                } else {
                    Log.w(tag, "Failed to get location.")
                }
            }
        } catch (exception: SecurityException) {
            Log.w(tag, "Lost location permission. $exception")
        }
    }

    private fun onNewLocation(location: Location) {
        Log.i(tag, "New location: $location")
        this.location = location
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
        val notificationIntent = Intent(this, FusedLocationUpdateService::class.java)
        notificationIntent.putExtra(extraStartedFromNotification, true)
        val servicePendingIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
        val title = getString(R.string.location_updated, DateFormat.getDateTimeInstance().format(Date()))
        val text = location?.getLocationText() ?: "Unknown location"
        val priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_HIGH else Notification.PRIORITY_HIGH
        return NotificationCompat.Builder(this, channelId)
            .addAction(R.drawable.ic_baseline_launch_24, getString(R.string.launch_activity), activityPendingIntent)
            .addAction(R.drawable.ic_baseline_cancel_24, getString(R.string.stop), servicePendingIntent)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setPriority(priority)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setChannelId(channelId)
            .build()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = updateIntervalMs
            fastestInterval = fastestUpdaterIntervalMs
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun serviceIsRunningInForeground(context: Context): Boolean {
        val activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for(service: ActivityManager.RunningServiceInfo in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(javaClass.name.equals(service.service.className)) {
                if(service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: FusedLocationUpdateService
            get() = this@FusedLocationUpdateService;
    }
}
