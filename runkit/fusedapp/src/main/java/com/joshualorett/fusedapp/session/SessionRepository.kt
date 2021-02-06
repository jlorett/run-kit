package com.joshualorett.fusedapp.session

import kotlinx.coroutines.flow.Flow

/**
 * The main point of access to the running session.
 * Created by Joshua on 12/29/2020.
 */
interface SessionRepository {
    val session: Flow<Session>
    fun start()
    fun pause()
    fun stop()
    fun connectSessionService(sessionService: SessionService)
    fun disconnectSessionService()
}