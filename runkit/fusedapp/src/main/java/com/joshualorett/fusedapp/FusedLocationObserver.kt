package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Listen for location updates from [FusedLocationUpdateService].
 * Created by Joshua on 8/19/2020.
 */
@ExperimentalCoroutinesApi
class FusedLocationObserver(private val context: Context, private val lifecycle: Lifecycle,
                            private val callback: (LocationData) -> Unit): LifecycleObserver {
    private val tag = FusedLocationObserver::class.java.simpleName
    private var locationUpdateService: FusedLocationUpdateService? = null
    private var bound = false
    private lateinit var receiver: FusedLocationUpdateReceiver

    // Monitors the state of the connection to the service.
    private val locationServiceConnection: ServiceConnection = object : ServiceConnection {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            lifecycle.coroutineScope.launch {
                val binder: FusedLocationUpdateService.FusedLocationUpdateServiceBinder = service as FusedLocationUpdateService.FusedLocationUpdateServiceBinder
                locationUpdateService = binder.service
                val sessionFlow = locationUpdateService?.sessionFlow
                val trackLocationFlow = locationUpdateService?.trackingLocationFlow
                sessionFlow?.combine(trackLocationFlow ?: throw NullPointerException()) { inSession, trackingLocation ->
                    val hasPermission = (context as AppCompatActivity).hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    if (inSession && hasPermission && !trackingLocation) {
                        startUpdates()
                    }
                    if (!hasPermission && inSession) {
                        stopUpdates()
                    }
                }?.first()
            }
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startUpdates() {
        locationUpdateService?.startLocationUpdates()
    }

    fun stopUpdates() {
        locationUpdateService?.stopLocationUpdates()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun create() {
        receiver = FusedLocationUpdateReceiver()
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