package com.joshualorett.fusedapp

import android.Manifest
import android.content.*
import android.location.Location
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * Listen for location updates from [FusedLocationUpdateService].
 * Created by Joshua on 8/19/2020.
 */
class FusedLocationObserver(private val context: Context, private val lifecycle: Lifecycle,
                            private val callback: (LocationData) -> Unit): LifecycleObserver {
    private var locationUpdateService: FusedLocationUpdateService? = null
    private var bound = false
    private lateinit var receiver: FusedLocationUpdateReceiver

    // Monitors the state of the connection to the service.
    private val locationServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: FusedLocationUpdateService.FusedLocationUpdateServiceBinder = service as FusedLocationUpdateService.FusedLocationUpdateServiceBinder
            locationUpdateService = binder.service
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationUpdateService = null
            bound = false
        }
    }

    /**
     * Adds [FusedLocationObserver] to a lifecycle so that it will be notified when the
     * LifecycleOwner changes state.
     */
    fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    /**
     * Removes [FusedLocationObserver] from the lifecycle's list of observers.
     */
    fun unregisterLifecycle(lifecycle: Lifecycle) {
        lifecycle.removeObserver(this)
    }

    fun startUpdates() {
        (context as AppCompatActivity).withPermission(Manifest.permission.ACCESS_FINE_LOCATION,
            run = {
                locationUpdateService?.requestLocationUpdates()
            },
            fallback = {
                error(LocationData.Error.PermissionError(SecurityException("Location permission missing.")))
            }
        )
    }

    fun stopUpdates() {
        locationUpdateService?.removeLocationUpdates()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun create() {
        receiver = FusedLocationUpdateReceiver()
        val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(context)
        if(isRequestingUpdates) {
            (context as AppCompatActivity).withPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                run = {},
                fallback = {
                    locationUpdateService?.removeLocationUpdates()
                    error(LocationData.Error.PermissionError(SecurityException("Location permission missing.")))
                })
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        context.bindService(Intent(context, FusedLocationUpdateService::class.java), locationServiceConnection, Context.BIND_AUTO_CREATE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun resume() {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(FusedLocationUpdateService.actionBroadcast))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun pause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stop() {
        if (bound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            context.unbindService(locationServiceConnection)
            bound = false
        }
    }

    private fun error(locationData: LocationData) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            callback(locationData)
        }
    }

    /**
     * Receiver for broadcasts sent by [FusedLocationUpdatesService].
     */
    private inner class FusedLocationUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(FusedLocationUpdateService.extraLocation)
            if (location != null) {
                if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    callback(LocationData.Success(location))
                }
            } else {
                error(LocationData.Error.MissingLocation)
            }
        }
    }

}