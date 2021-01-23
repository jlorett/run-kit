package com.joshualorett.fusedapp.session

import android.content.Context
import android.location.Location
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
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
    private const val stopped = 0
    private const val started = 1
    private const val paused = 2
    private lateinit var roomDao: RoomSessionDao
    private lateinit var dataStore: DataStore<Preferences>
    private val sessionStateKey = preferencesKey<Int>("sessionState")
    private val distanceKey = preferencesKey<Float>("distance")
    private val timeKey = preferencesKey<Long>("time")
    override var initialized = false
    private var sessionId = MutableStateFlow(0L)

    fun init(context: Context, roomSessionDao: RoomSessionDao) {
        if (!initialized) {
            roomDao = roomSessionDao
            dataStore = context.createDataStore(name = "session")
            initialized = true
        }
    }

    override fun getSessionStateFlow(): Flow<Session.State> {
        return dataStore.data.map { preferences ->
            when(preferences[sessionStateKey]) {
                stopped -> Session.State.STOPPED
                started -> Session.State.STARTED
                paused -> Session.State.PAUSED
                else -> Session.State.STOPPED
            }
        }
    }

    override fun getElapsedTimeFlow(): Flow<Long> {
        return sessionId.flatMapConcat {
            if(it == 0L) {
                flowOf(0L)
            } else {
                roomDao.getSessionElapsedTime(it)
            }
        }
    }

    override fun getDistanceFlow(): Flow<Float> {
        return sessionId.flatMapConcat {
            if(it == 0L) {
                flowOf(0F)
            } else {
                roomDao.getSessionDistance(sessionId.value)
            }
        }
    }

    override suspend fun setSessionState(sessionState: Session.State) {
        when(sessionState) {
            Session.State.STARTED -> {
                dataStore.edit { preferences ->
                    preferences[sessionStateKey] = started
                }
                if(sessionId.value == 0L) {
                    createSession()
                }
            }
            Session.State.STOPPED -> {
                dataStore.edit { preferences ->
                    preferences.remove(sessionStateKey)
                    preferences.remove(distanceKey)
                    preferences.remove(timeKey)
                }
                sessionId.value = 0L
            }
            Session.State.PAUSED -> {
                dataStore.edit { preferences ->
                    preferences[sessionStateKey] = paused
                }
            }
        }
    }

    override suspend fun setElapsedTime(time: Long) {
        if(sessionId.value > 0) {
            roomDao.updateSessionElapsedTime(sessionId.value, time)
        }
    }

    override suspend fun setDistance(distance: Float) {
        if(sessionId.value > 0) {
            roomDao.updateSessionDistance(sessionId.value, distance)
        }
    }

    override suspend fun createSession(title: String?): Long {
        val sessionEntity = SessionEntity(0, getDateForDatabase(Date()), title, 0F, 0L)
        sessionId.value = roomDao.createSession(sessionEntity)
        return sessionId.value
    }

    override suspend fun addSessionLocation(location: Location) {
        val locationEntity = toLocationEntity(sessionId.value, location)
        roomDao.addLocation(locationEntity)
    }

    override suspend fun getSessionLocations(): List<String> {
        val sessionWithLocations = roomDao.getSessionWithLocations().single()
        return sessionWithLocations.map { entity -> entity.toString() }
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