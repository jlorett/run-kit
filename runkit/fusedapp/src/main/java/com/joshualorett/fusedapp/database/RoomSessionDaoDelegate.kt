package com.joshualorett.fusedapp.database

import android.location.Location
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionDao
import com.joshualorett.fusedapp.toIsoString
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * Delegates commands to the [RoomSessionDao].
 * Created by Joshua on 9/27/2020.
 */
object RoomSessionDaoDelegate: SessionDao {
    private lateinit var roomDao: RoomSessionDao
    var initialized = false

    fun init(roomSessionDao: RoomSessionDao) {
        if (!initialized) {
            roomDao = roomSessionDao
            initialized = true
        }
    }

    override fun getSessionFlow(): Flow<Session> {
        return roomDao.getCurrentSession().map { sessionEntity ->
                sessionEntity?.toSession() ?: Session()
            }
    }

    override suspend fun setSessionState(sessionState: Session.State) {
        var sessionId = getCurrentSessionId()
        when(sessionState) {
            Session.State.STARTED -> {
                if(sessionId == 0L) {
                    sessionId = createSession()
                }
                roomDao.updateSessionState(sessionId, Session.State.STARTED)
            }
            Session.State.STOPPED -> {
                roomDao.updateSessionState(sessionId, Session.State.STOPPED)
            }
            Session.State.PAUSED -> {
                roomDao.updateSessionState(sessionId, Session.State.PAUSED)
            }
        }
    }

    override suspend fun setElapsedTime(time: Long) {
        val sessionId = getCurrentSessionId()
        if(sessionId > 0) {
            roomDao.updateSessionElapsedTime(sessionId, time)
        }
    }

    override suspend fun setDistance(distance: Float) {
        val sessionId = getCurrentSessionId()
        if(sessionId > 0) {
            roomDao.updateSessionDistance(sessionId, distance)
        }
    }

    override suspend fun addSessionLocation(location: Location) {
        val sessionId = getCurrentSessionId()
        val locationEntity = toLocationEntity(sessionId, location)
        roomDao.addLocation(locationEntity)
    }

    override suspend fun getSessionLocations(): List<String> {
        val sessionWithLocations = roomDao.getSessionWithLocations().first()
        return sessionWithLocations.map { entity -> entity.toString() }
    }

    private suspend fun getCurrentSessionId(): Long {
        return roomDao.getCurrentSession().first()?.id ?: 0
    }

    private suspend fun createSession(title: String? = null): Long {
        val sessionEntity = SessionEntity(0, Date().toIsoString(), title, 0F, 0L,
            Session.State.STOPPED
        )
        return roomDao.createSession(sessionEntity)
    }

    private fun toLocationEntity(sessionId: Long, location: Location): LocationEntity {
        return LocationEntity(
            sessionId,
            Date(location.time).toIsoString(),
            location.latitude,
            location.longitude
        )

    }
}