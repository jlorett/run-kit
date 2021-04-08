package com.joshualorett.fusedapp.session

import com.joshualorett.runkit.session.Session
import kotlinx.coroutines.flow.Flow

/**
 * Interact with a location tracking session.
 * Created by Joshua on 11/14/2020.
 */
interface SessionService {
    fun start()
    fun stop()
    fun pause()
    fun session(): Flow<Session>
}