package com.joshualorett.fusedapp.location

import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*

/**
 * Track locations from [FusedLocationProviderClient].
 * Created by Joshua on 9/7/2020.
 */
class FusedLocationTracker(private val fusedLocationClient: FusedLocationProviderClient):
    LocationTracker {
    // todo move to config
    private val updateIntervalMs: Long = 10000
    private val fastestUpdaterIntervalMs = updateIntervalMs / 2
    private val locationRequest = LocationRequest().apply {
        interval = updateIntervalMs
        fastestInterval = fastestUpdaterIntervalMs
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    var location: Location? = null

    override fun track(): Flow<Location> {
        return getLocationUpdates()
            .combine(getLastLocation().filterNotNull()) { lastLocation, location ->
                if (location.time > lastLocation.time) {
                    location
                } else {
                    lastLocation
                }
            }
            .conflate()
            .onEach { location -> this.location = location }
    }

    private fun getLocationUpdates(): Flow<Location> {
        return callbackFlow {
            val locationCallback = object: LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    locationResult?.lastLocation?.let {
                        sendBlocking(it)
                    }
                }
            }
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            } catch (exception: SecurityException) {
                cancel("Lost location permission. Could not request updates. $exception")
            }

            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun getLastLocation(): Flow<Location?> {
        return callbackFlow {
             val onComplete: OnCompleteListener<Location> = OnCompleteListener<Location> { task ->
                 if(task.isSuccessful && task.result != null) {
                     sendBlocking(task.result)
                 } else {
                     sendBlocking(null)
                 }
             }
            try {
                fusedLocationClient.lastLocation.addOnCompleteListener(onComplete)
            } catch (exception: SecurityException) {
                cancel("Lost location permission. Could not request updates. $exception")
            }

            awaitClose {
                cancel()
            }
        }
    }
}