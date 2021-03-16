package com.joshualorett.fusedapp.session

import android.location.Location

/**
 * Data access object for a session.
 * Created by Joshua on 9/27/2020.
 */
interface SessionDao {
    suspend fun createSession(): Long
    suspend fun setSessionState(id: Long, sessionState: Session.State)
    suspend fun setElapsedTime(id: Long, time: Long)
    suspend fun setDistance(id: Long, distance: Float)
    suspend fun setEndTime(id: Long, endTime: String)
    suspend fun addLocation(id: Long, location: Location)
}