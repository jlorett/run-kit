package com.joshualorett.runkit.session

import com.joshualorett.runkit.location.LocationTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Check if a session is in a valid state. Useful to check after things like rebinding a service.
 * Created by Joshua on 3/30/2021.
 */
class SessionMonitor(
    private val activeSessionRepository: ActiveSessionRepository,
    private val locationTracker: LocationTracker
) {

    suspend fun checkSession(hasLocationPermission: () -> Boolean,
                             restartSession: () -> Unit,
                             pauseSession: () -> Unit) {
        val locationPermitted = hasLocationPermission()
        val inSession = withContext(Dispatchers.Default) {
            activeSessionRepository.session.first().state == Session.State.STARTED
        }
        val trackingLocation = locationTracker.trackingLocation
        // Tracking stopped, restarting location tracking.
        if (inSession && locationPermitted && !trackingLocation) {
            restartSession()
        }
        // Permission lost, pause session.
        if (!locationPermitted) {
            pauseSession()
        }
    }
}
