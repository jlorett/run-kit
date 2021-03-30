package com.joshualorett.runkit.location

import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onCompletion

/**
 * Track location with the system location framework.
 * Created by Joshua on 3/29/2021.
 */
class SystemLocationTracker(private val locationManager: LocationManager,
                            private val settings: LocationIntervalSettings) : LocationTracker {
    override var trackingLocation = false

    override fun track(): Flow<Location> {
        return getLocationUpdates()
            .conflate()
            .onCompletion {
                trackingLocation = false
            }
    }

    private fun getLocationUpdates(): Flow<Location> {
        return callbackFlow {
            val locationCallback = object : LocationListener {
                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                override fun onLocationChanged(location: Location) {
                    ensureActive()
                    offer(location)
                }
            }
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            criteria.isAltitudeRequired = true
            val provider: String = locationManager.getBestProvider(criteria, false) ?: ""
            if(provider.isEmpty()) {
                cancel("Provider unavailable.")
            }
            try {
                locationManager.requestLocationUpdates(provider, settings.minimumMilliseconds, settings.minimumMeters, locationCallback)
                trackingLocation = true
            } catch (exception: SecurityException) {
                cancel("Lost location permission. Could not request updates. $exception")
            }
            awaitClose {
                locationManager.removeUpdates(locationCallback)
            }
        }
    }
}