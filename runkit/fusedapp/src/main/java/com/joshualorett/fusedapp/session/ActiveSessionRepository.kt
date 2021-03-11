package com.joshualorett.fusedapp.session

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * The main point of access to the active running session.
 * Created by Joshua on 2/12/2021.
 */
class ActiveSessionRepository(private val sessionDao: SessionDao,
                              private val activeSessionDao: ActiveSessionDao): SessionRepository {
    override val session: Flow<Session> = activeSessionDao.getActiveSessionFlow()

    override suspend fun start() {
        val id = getCurrentSessionId()
        sessionDao.setSessionState(id, Session.State.STARTED)
    }

    override suspend fun pause() {
        val id = getCurrentSessionId()
        sessionDao.setSessionState(id, Session.State.PAUSED)
    }

    override suspend fun stop()  {
        val id = getCurrentSessionId()
        sessionDao.setSessionState(id, Session.State.STOPPED)
    }

    override suspend fun setElapsedTime(time: Long) {
        val id = getCurrentSessionId()
        sessionDao.setElapsedTime(id, time)
    }

    override suspend fun setDistance(distance: Float) {
        val id = getCurrentSessionId()
        sessionDao.setDistance(id, distance)
    }

    override suspend fun addLocation(location: Location) {
        val id = getCurrentSessionId()
        sessionDao.addLocation(id, location)
    }

    private suspend fun getCurrentSessionId(): Long {
        return activeSessionDao.getActiveSessionFlow().first().id
    }
}