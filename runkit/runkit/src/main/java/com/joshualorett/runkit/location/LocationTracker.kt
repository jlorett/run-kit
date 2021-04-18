package com.joshualorett.runkit.location

import kotlinx.coroutines.flow.Flow

/**
 * Track location using [Flow].
 *
 * Implementations should unregister from location services when the flow is cancelled.
 * Created by Joshua on 3/29/2020.
 */
interface LocationTracker {
    val trackingLocation: Boolean
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