package com.joshualorett.fusedapp.database

import android.location.Location
import com.joshualorett.fusedapp.database.location.LocationEntity
import com.joshualorett.fusedapp.database.location.RoomLocationDao
import com.joshualorett.fusedapp.session.SessionDao
import com.joshualorett.fusedapp.toIsoString
import com.joshualorett.runkit.session.Session
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

    override suspend fun createSession(): Long {
        val sessionEntity = SessionEntity(0, Date().toIsoString(), null, null,
            0.0, 0L, Session.State.STOPPED
        )
        return sessionDao.createSession(sessionEntity)
    }

    override suspend fun setSessionState(id: Long, sessionState: Session.State) {
        if(id > 0) {
            sessionDao.updateSessionState(id, sessionState)
        }
    }

    override suspend fun setElapsedTime(id: Long, time: Long) {
        if(id > 0) {
            sessionDao.updateSessionElapsedTime(id, time)
        }
    }

    override suspend fun setDistance(id: Long, distance: Double) {
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
        locationDao.addLocation(LocationEntity(id, location))
    }
}