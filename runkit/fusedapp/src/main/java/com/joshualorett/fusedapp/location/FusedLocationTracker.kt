package com.joshualorett.fusedapp.location

import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Track locations from [FusedLocationProviderClient].
 * Created by Joshua on 9/7/2020.
 */
@ExperimentalCoroutinesApi
class FusedLocationTracker(private val fusedLocationClient: FusedLocationProviderClient,
                           private val looper: Looper, private val settings: FusedLocationSettings = FusedLocationSettings()):
    LocationTracker {
    private val locationRequest = LocationRequest().apply {
        interval = settings.updateInterval
        fastestInterval = settings.fastestUpdateInterval
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private var _trackingLocation = MutableStateFlow(false)
    override val trackingLocation: StateFlow<Boolean> = _trackingLocation

    /***
     * Track location as a Flow using [callbackFlow]. This will automatically stop tracking
     * once the flow is cancelled or completed.
     */
    override fun track(): Flow<Location> {
        return getLocationUpdates()
            .conflate()
            .onCompletion {
                _trackingLocation.value = false
            }
    }

    private fun getLocationUpdates(): Flow<Location> {
        return callbackFlow {
            val locationCallback = object: LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    locationResult?.lastLocation?.let {
                        offer(it)
                    }
                }
            }
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, looper)
                _trackingLocation.value = true
            } catch (exception: SecurityException) {
                cancel("Lost location permission. Could not request updates. $exception")
            }
            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}