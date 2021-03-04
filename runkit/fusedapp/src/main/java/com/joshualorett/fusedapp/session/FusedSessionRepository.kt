package com.joshualorett.fusedapp.session

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * The main point of access to the fused running session.
 * Created by Joshua on 2/12/2021.
 */
class FusedSessionRepository(private val sessionDao: SessionDao): SessionRepository {
    override val session: Flow<Session> = sessionDao.getActiveSessionFlow()

    override suspend fun start() {
        sessionDao.setSessionState(Session.State.STARTED)
    }

    override suspend fun pause() {
        sessionDao.setSessionState(Session.State.PAUSED)
    }

    override suspend fun stop()  {
        sessionDao.setSessionState(Session.State.STOPPED)
    }

    override suspend fun setElapsedTime(time: Long) {
        sessionDao.setElapsedTime(time)
    }

    override suspend fun setDistance(distance: Float) {
        sessionDao.setDistance(distance)
    }

    override suspend fun addSessionLocation(location: Location) {
        sessionDao.addSessionLocation(location)
    }

    override suspend fun getSessionLocations(): List<String> {
        return sessionDao.getSessionLocations()
    }
}