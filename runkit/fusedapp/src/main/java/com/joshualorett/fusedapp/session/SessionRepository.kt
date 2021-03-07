package com.joshualorett.fusedapp.session

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * The main point of access to the running session.
 * Created by Joshua on 12/29/2020.
 */
interface SessionRepository {
    val session: Flow<Session>
    suspend fun start()
    suspend fun pause()
    suspend fun stop()
    suspend fun setElapsedTime(time: Long)
    suspend fun setDistance(distance: Float)
    suspend fun addLocation(location: Location)
    suspend fun getSessionLocations(): List<String>
}