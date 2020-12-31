package com.joshualorett.fusedapp.location

import android.location.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Track location using [Flow].
 *
 * Implementations should unregister from location services when the flow is cancelled.
 * Created by Joshua on 9/7/2020.
 */
interface LocationTracker {
    @ExperimentalCoroutinesApi
    val trackingLocation: StateFlow<Boolean>
    /***
     * Streams location events. To stop tracking location, cancel the flow.
     *
     * @sample
     *  scope.launch {
     *      val childJob = locationTracker.track().collect()
     *      childJob.cancel() // stop location tracking.
     *  }
     */
    fun track(): Flow<Location>
}