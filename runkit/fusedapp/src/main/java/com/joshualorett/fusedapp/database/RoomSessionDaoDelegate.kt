package com.joshualorett.fusedapp.database

import android.location.Location
import com.joshualorett.fusedapp.database.location.LocationEntity
import com.joshualorett.fusedapp.database.location.RoomLocationDao
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionDao
import com.joshualorett.fusedapp.toIsoString
import java.util.*

/**
 * Delegates commands to the [RoomSessionDao].
 * Created by Joshua on 9/27/2020.
 */
object RoomSessionDaoDelegate: SessionDao {
    private lateinit var sessionDao: RoomSessionDao
    private lateinit var locationDao: RoomLocationDao
    var initialized = false

    fun init(roomSessionDao: RoomSessionDao,
            locationDao: RoomLocationDao) {
        if (!initialized) {
            this.sessionDao = roomSessionDao
            this.locationDao = locationDao
            initialized = true
        }
    }

    override suspend fun setSessionState(id: Long, sessionState: Session.State) {
        when(sessionState) {
            Session.State.STARTED -> {
                val sessionId = if(id == 0L) createSession() else id
                sessionDao.updateSessionState(sessionId, Session.State.STARTED)
            }
            Session.State.STOPPED -> {
                sessionDao.updateSessionState(id, Session.State.STOPPED)
            }
            Session.State.PAUSED -> {
                sessionDao.updateSessionState(id, Session.State.PAUSED)
            }
        }
    }

    override suspend fun setElapsedTime(id: Long, time: Long) {
        if(id > 0) {
            sessionDao.updateSessionElapsedTime(id, time)
        }
    }

    override suspend fun setDistance(id: Long, distance: Float) {
        if(id > 0) {
            sessionDao.updateSessionDistance(id, distance)
        }
    }

    override suspend fun setEndTime(id: Long, endTime: String) {
        if(id > 0) {
            sessionDao.updateSessionEndTime(id, endTime)
        }
    }

    override suspend fun addLocation(id: Long, location: Location) {
        val locationEntity = toLocationEntity(id, location)
        locationDao.addLocation(locationEntity)
    }

    private suspend fun createSession(title: String? = null): Long {
        val sessionEntity = SessionEntity(0, Date().toIsoString(), null, title,
            0F, 0L, Session.State.STOPPED
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