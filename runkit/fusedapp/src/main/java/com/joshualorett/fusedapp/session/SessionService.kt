package com.joshualorett.fusedapp.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interact with a location tracking session.
 * Created by Joshua on 11/14/2020.
 */
interface SessionService {
    @ExperimentalCoroutinesApi
    val session: StateFlow<Session>
    val elapsedTime: Flow<Long>
    fun start()
    fun stop()
    fun pause()
    fun trackingLocation(): Boolean
}