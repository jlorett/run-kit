package com.joshualorett.fusedapp.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Data storage for session data.
 * Created by Joshua on 9/27/2020.
 */
object SessionDataStore: SessionDao {
    private const val stopped = 0
    private const val started = 1
    private const val paused = 2
    private lateinit var dataStore: DataStore<Preferences>
    private val sessionStateKey = preferencesKey<Int>("sessionState")
    private val distanceKey = preferencesKey<Float>("distance")
    private val timeKey = preferencesKey<Long>("time")
    override var initialized = false

    fun init(context: Context) {
        if (!initialized) {
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
}