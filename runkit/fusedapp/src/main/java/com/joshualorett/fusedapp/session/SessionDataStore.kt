package com.joshualorett.fusedapp.session

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Data storage for Session data.
 * Created by Joshua on 9/27/2020.
 */
object SessionDataStore: SessionDao {
    private lateinit var dataStore: DataStore<Preferences>
    private val inSessionKey = preferencesKey<Boolean>("inSession")
    val initialized = ::dataStore.isInitialized

    fun init(context: Context) {
        if (!initialized) {
            dataStore = context.createDataStore(name = "session")
        }
    }

    override fun getSessionFlow(): Flow<Boolean> {
        return dataStore.data.map{ preferences ->
            preferences[inSessionKey] ?: false
        }
    }

    override suspend fun setInSession(inSession: Boolean) {
        dataStore.edit { preferences ->
            preferences[inSessionKey] = inSession
        }
    }
}