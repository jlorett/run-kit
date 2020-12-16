package com.joshualorett.fusedapp.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    override fun getSessionFlow(): Flow<Session> {
        return dataStore.data.map{ preferences ->
            val state = when(preferences[sessionStateKey]) {
                stopped -> Session.State.STOPPED
                started -> Session.State.STARTED
                paused -> Session.State.PAUSED
                else -> Session.State.STOPPED
            }
            val distance = preferences[distanceKey] ?: 0F
            val time = preferences[timeKey] ?: 0L
            Session(time, distance, state)
        }
    }

    override suspend fun setSession(session: Session) {
        when(session.state) {
            Session.State.STARTED -> {
                val currentDistance = getSessionFlow().first().distance
                dataStore.edit { preferences ->
                    preferences[sessionStateKey] = started
                    preferences[distanceKey] = currentDistance + session.distance
                    preferences[timeKey] = session.time
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
                    preferences[timeKey] = session.time
                }
            }
        }
    }
}