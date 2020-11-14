package com.joshualorett.fusedapp

import android.Manifest
import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.joshualorett.fusedapp.session.Session
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Listen for location updates from [FusedLocationUpdateService].
 * Created by Joshua on 8/19/2020.
 */
@ExperimentalCoroutinesApi
class FusedLocationObserver(private val context: Context, private val lifecycle: Lifecycle): LifecycleObserver {
    private val tag = FusedLocationObserver::class.java.simpleName
    private var locationUpdateService: FusedLocationUpdateService? = null
    private var bound = false
    private var _sessionState = MutableStateFlow(Session())
    var sessionFlow: StateFlow<Session> = _sessionState

    // Monitors the state of the connection to the service.
    private val locationServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: FusedLocationUpdateService.FusedLocationUpdateServiceBinder = service as FusedLocationUpdateService.FusedLocationUpdateServiceBinder
            locationUpdateService = binder.service
            lifecycle.coroutineScope.launch {
                checkSessionState()
                locationUpdateService?.sessionFlow?.collect { session ->
                    _sessionState.value = session
                }
            }
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationUpdateService = null
            bound = false
        }
    }

    private suspend fun checkSessionState(): Boolean {
        val hasPermission =
            (context as AppCompatActivity).hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val inSession = locationUpdateService?.sessionFlow?.first()?.state == Session.State.STARTED
        val trackingLocation = locationUpdateService?.trackingLocationFlow?.value == true
        if (inSession && hasPermission && !trackingLocation) {
            Log.d(tag, "Tracking stopped, restarting location tracking.")
            try {
                startUpdates()
            } catch (e: SecurityException) {
                stopUpdates()
            }
            return false
        }
        if (!hasPermission && inSession) {
            Log.d(tag, "Permission lost, stopping location tracking.")
            stopUpdates()
            return false
        }
        return true
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
        locationUpdateService?.start()
    }

    fun stopUpdates() {
        locationUpdateService?.stop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        context.bindService(Intent(context, FusedLocationUpdateService::class.java), locationServiceConnection, Context.BIND_AUTO_CREATE)
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroy() {
        unregisterLifecycle(lifecycle)
    }
}