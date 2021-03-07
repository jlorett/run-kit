package com.joshualorett.fusedapp.database

import android.location.Location
import com.joshualorett.fusedapp.database.active.RoomActiveSessionDao
import com.joshualorett.fusedapp.database.location.LocationEntity
import com.joshualorett.fusedapp.database.location.RoomLocationDao
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
    private lateinit var sessionDao: RoomSessionDao
    private lateinit var activeSessionDao: RoomActiveSessionDao
    private lateinit var locationDao: RoomLocationDao
    var initialized = false

    fun init(roomSessionDao: RoomSessionDao,
             activeSessionDao: RoomActiveSessionDao,
            locationDao: RoomLocationDao) {
        if (!initialized) {
            this.sessionDao = roomSessionDao
            this.activeSessionDao = activeSessionDao
            this.locationDao = locationDao
            initialized = true
        }
    }

    override fun getActiveSessionFlow(): Flow<Session> {
        return activeSessionDao.getActiveSession().map { sessionEntity ->
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
                sessionDao.updateSessionState(sessionId, Session.State.STARTED)
            }
            Session.State.STOPPED -> {
                sessionDao.updateSessionState(sessionId, Session.State.STOPPED)
            }
            Session.State.PAUSED -> {
                sessionDao.updateSessionState(sessionId, Session.State.PAUSED)
            }
        }
    }

    override suspend fun setElapsedTime(time: Long) {
        val sessionId = getCurrentSessionId()
        if(sessionId > 0) {
            sessionDao.updateSessionElapsedTime(sessionId, time)
        }
    }

    override suspend fun setDistance(distance: Float) {
        val sessionId = getCurrentSessionId()
        if(sessionId > 0) {
            sessionDao.updateSessionDistance(sessionId, distance)
        }
    }

    override suspend fun addLocation(location: Location) {
        val sessionId = getCurrentSessionId()
        val locationEntity = toLocationEntity(sessionId, location)
        locationDao.addLocation(locationEntity)
    }

    override suspend fun getSessionLocations(): List<String> {
        val sessionWithLocations = sessionDao.getSessionWithLocations().first()
        return sessionWithLocations.map { entity -> entity.toString() }
    }

    private suspend fun getCurrentSessionId(): Long {
        return activeSessionDao.getActiveSession().first()?.id ?: 0
    }

    private suspend fun createSession(title: String? = null): Long {
        val sessionEntity = SessionEntity(0, Date().toIsoString(), title, 0F, 0L,
            Session.State.STOPPED
        )
        return sessionDao.createSession(sessionEntity)
    }

    private fun toLocationEntity(sessionId: Long, location: Location): LocationEntity {
        return LocationEntity(
            sessionId,
            Date(location.time).toIsoString(),
            location.latitude,
            location.longitude,
            location.altitude
        )
    }
}