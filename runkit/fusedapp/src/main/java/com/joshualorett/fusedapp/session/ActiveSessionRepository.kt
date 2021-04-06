package com.joshualorett.fusedapp.session

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * The main point of access to the running session.
 * Created by Joshua on 12/29/2020.
 */
interface ActiveSessionRepository {
    val session: Flow<Session>
    fun start(scope: CoroutineScope)
    suspend fun pause()
    suspend fun stop()
    suspend fun setElapsedTime(time: Long)
    suspend fun setDistance(distance: Double)
    suspend fun addLocation(location: Location)
}