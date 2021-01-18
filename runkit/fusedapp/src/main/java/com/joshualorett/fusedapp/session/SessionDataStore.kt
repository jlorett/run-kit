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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        return dataStore.data.map { preferences ->
            preferences[timeKey] ?: 0L
        }
    }

    override fun getDistanceFlow(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[distanceKey] ?: 0F
        }
    }

    override suspend fun setSessionState(sessionState: Session.State) {
        when(sessionState) {
            Session.State.STARTED -> {
                dataStore.edit { preferences ->
                    preferences[sessionStateKey] = started
                }
            }
            Session.State.STOPPED -> {
                dataStore.edit { preferences ->
                    preferences.remove(sessionStateKey)
                    preferences.remove(distanceKey)
                    preferences.remove(timeKey)
                }
            }
            Session.State.PAUSED -> {
                dataStore.edit { preferences ->
                    preferences[sessionStateKey] = paused
                }
            }
        }
    }

    override suspend fun setElapsedTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[timeKey] = time
        }
    }

    override suspend fun setDistance(distance: Float) {
        dataStore.edit { preferences ->
            preferences[distanceKey] = distance
        }
    }

    override suspend fun createSession(title: String?): Long {
        val sessionEntity = SessionEntity(0, getDateForDatabase(Date()), title)
        return roomDao.createSession(sessionEntity)
    }

    override suspend fun addSessionLocation(sessionId: Long, location: Location) {
        val locationEntity = toLocationEntity(sessionId, location)
        roomDao.addLocation(locationEntity)
    }

    override suspend fun getSessionLocations(): List<String> {
        val sessionWithLocations = roomDao.getSessionWithLocations()
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