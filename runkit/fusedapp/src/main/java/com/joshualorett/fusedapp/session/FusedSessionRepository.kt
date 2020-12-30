package com.joshualorett.fusedapp.session

import kotlinx.coroutines.flow.*

/**
 * The main point of access to the running session.
 * Created by Joshua on 12/29/2020.
 */
class FusedSessionRepository(sessionDao: SessionDao): SessionRepository {
    private var sessionService: SessionService? = null
    private val sessionStateFlow = sessionDao.getSessionStateFlow()
    private val distanceFlow = sessionDao.getDistanceFlow()
    private val timeFlow = sessionDao.getElapsedTimeFlow()
    override val elapsedTime: Flow<Long> = sessionDao.getElapsedTimeFlow()
    override val session: Flow<Session> = combine(sessionStateFlow, timeFlow, distanceFlow) { state: Session.State, time: Long, distance: Float ->
        Session(time, distance, state)
    }.distinctUntilChanged()

    override fun connectSessionService(sessionService: SessionService) {
        this.sessionService = sessionService
    }

    override fun disconnectSessionService() {
        this.sessionService = null
    }

    override fun start() {
        if(sessionService == null) {
            throw IllegalStateException("The SessionService must be connected first.")
        }
        try {
            sessionService?.start()
        } catch (e: SecurityException) {
            stop()
        }
    }

    override fun pause() {
        if(sessionService == null) {
            throw IllegalStateException("The SessionService must be connected first.")
        }
        sessionService?.pause()
    }

    override fun stop() {
        if(sessionService == null) {
            throw IllegalStateException("The SessionService must be connected first.")
        }
        sessionService?.stop()
    }
}