package com.joshualorett.fusedapp.session

import kotlinx.coroutines.flow.Flow

/**
 * Interact with a location tracking session.
 * Created by Joshua on 11/14/2020.
 */
interface SessionService {
    val sessionFlow: Flow<Session>
    fun start()
    fun stop()
    fun pause()
    fun trackingLocation(): Boolean
    suspend fun inSession(): Boolean
}