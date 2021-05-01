package com.joshualorett.runkit.session

import com.joshualorett.runkit.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for a session.
 * Created by Joshua on 9/27/2020.
 */
interface SessionDao {
    fun getSessions(): Flow<List<Session>>
    suspend fun createSession(): Long
    suspend fun setSessionState(id: Long, sessionState: Session.State)
    suspend fun setElapsedTime(id: Long, time: Long)
    suspend fun setDistance(id: Long, distance: Double)
    suspend fun setEndTime(id: Long, endTime: String)
    suspend fun addLocation(id: Long, location: Location)
}
