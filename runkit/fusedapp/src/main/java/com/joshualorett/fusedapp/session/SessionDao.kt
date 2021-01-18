package com.joshualorett.fusedapp.session

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for a session.
 * Created by Joshua on 9/27/2020.
 */
interface SessionDao {
    val initialized: Boolean
    fun getSessionStateFlow(): Flow<Session.State>
    fun getElapsedTimeFlow(): Flow<Long>
    fun getDistanceFlow(): Flow<Float>
    suspend fun setSessionState(sessionState: Session.State)
    suspend fun setElapsedTime(time: Long)
    suspend fun setDistance(distance: Float)
    suspend fun createSession(title: String? = null): Long
    suspend fun addSessionLocation(sessionId: Long, location: Location)
    suspend fun getSessionLocations(): List<String>
}