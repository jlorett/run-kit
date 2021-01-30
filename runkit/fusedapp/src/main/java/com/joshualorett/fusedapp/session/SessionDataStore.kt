package com.joshualorett.fusedapp.session

import android.location.Location
import com.joshualorett.fusedapp.database.LocationEntity
import com.joshualorett.fusedapp.database.RoomSessionDao
import com.joshualorett.fusedapp.database.SessionEntity
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data storage for session data.
 * Created by Joshua on 9/27/2020.
 */
object SessionDataStore: SessionDao {
    private lateinit var roomDao: RoomSessionDao
    var initialized = false

    fun init(roomSessionDao: RoomSessionDao) {
        if (!initialized) {
            roomDao = roomSessionDao
            initialized = true
        }
    }

    override fun getSessionStateFlow(): Flow<Session.State> {
        return roomDao.getCurrentSession().flatMapLatest { sessionEntity ->
            val it = sessionEntity?.id ?: 0L
            if(it == 0L) {
                flowOf(Session.State.STOPPED)
            } else {
                roomDao.getSessionState(it)
            }
        }
    }

    override fun getElapsedTimeFlow(): Flow<Long> {
        return roomDao.getCurrentSession().flatMapLatest { sessionEntity ->
            val it = sessionEntity?.id ?: 0L
            if(it == 0L || sessionEntity?.state == Session.State.STOPPED) {
                flowOf(0L)
            } else {
                roomDao.getSessionElapsedTime(it)
            }
        }
    }

    override fun getDistanceFlow(): Flow<Float> {
        return roomDao.getCurrentSession().flatMapLatest { sessionEntity ->
            val it = sessionEntity?.id ?: 0L
            if(it == 0L) {
                flowOf(0F)
            } else {
                roomDao.getSessionDistance(it)
            }
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

    private fun createSession(title: String? = null): Long {
        val sessionEntity = SessionEntity(0, getDateForDatabase(Date()), title, 0F, 0L, Session.State.STOPPED)
        return roomDao.createSession(sessionEntity)
    }

    private fun toLocationEntity(sessionId: Long, location: Location): LocationEntity {
        return LocationEntity(
            sessionId,
            getDateForDatabase(Date(location.time)),
            location.latitude,
            location.longitude
        )

    }

    private fun getDateForDatabase(date: Date): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date.toInstant().toString()
        } else {
            SimpleDateFormat("YYYY-MM-DDTHH:MM:SS.SSSZ").format(date)
        }
    }
}