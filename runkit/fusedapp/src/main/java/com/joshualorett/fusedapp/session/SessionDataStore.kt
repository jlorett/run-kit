package com.joshualorett.fusedapp.session

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Data storage for session data.
 * Created by Joshua on 9/27/2020.
 */
object SessionDataStore: SessionDao {
    private lateinit var dataStore: DataStore<Preferences>
    private val inSessionKey = preferencesKey<Boolean>("inSession")
    private val distanceKey = preferencesKey<Float>("distance")
    override val initialized = ::dataStore.isInitialized

    fun init(context: Context) {
        if (!initialized) {
            dataStore = context.createDataStore(name = "session")
        }
    }

    override fun getSessionFlow(): Flow<Session> {
        return dataStore.data.map{ preferences ->
            val state = when(preferences[inSessionKey]) {
                true -> Session.State.STARTED
                false -> Session.State.STOPPED
                null -> Session.State.IDLE
            }
            val distance = preferences[distanceKey] ?: 0F
            Session(0, distance, state)
        }
    }

    override suspend fun setSession(session: Session) {
        if (session.state == Session.State.IDLE) {
            dataStore.edit { preferences ->
                preferences.remove(inSessionKey)
                preferences.remove(distanceKey)
            }
        } else {
            val currentDistance = getSessionFlow().first().distance
            dataStore.edit { preferences ->
                preferences[inSessionKey] = session.state == Session.State.STARTED
                preferences[distanceKey] = currentDistance + session.distance
            }
        }
    }
}