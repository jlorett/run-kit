package com.joshualorett.fusedapp.distance

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Data access object for distance.
 * Created by Joshua on 10/18/2020.
 */
object DistanceDataStore: DistanceDao {
    private lateinit var dataStore: DataStore<Preferences>
    private val distanceKey = preferencesKey<Float>("distance")
    override val initialized = ::dataStore.isInitialized

    fun init(context: Context) {
        if (!initialized) {
            dataStore = context.createDataStore(name = "sessionDistance")
        }
    }

    override fun getDistanceFlow(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[distanceKey] ?: 0F
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(distanceKey)
        }
    }

    override suspend fun updateDistance(distance: Float) {
        val currentDistance = getDistanceFlow().first()
        dataStore.edit { preferences ->
            preferences[distanceKey] = currentDistance + distance
        }
    }
}