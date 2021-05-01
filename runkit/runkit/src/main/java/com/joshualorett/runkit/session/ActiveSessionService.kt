package com.joshualorett.runkit.session

import kotlinx.coroutines.flow.Flow

/**
 * Interact with the active session.
 * Created by Joshua on 11/14/2020.
 */
interface ActiveSessionService {
    fun start()
    fun stop()
    fun pause()
    fun session(): Flow<Session>
}
