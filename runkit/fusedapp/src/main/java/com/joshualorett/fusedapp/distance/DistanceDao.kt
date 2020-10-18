package com.joshualorett.fusedapp.distance

import kotlinx.coroutines.flow.Flow

/**
 * Track and update distance for the active session.
 * Created by Joshua on 10/18/2020.
 */
interface DistanceDao {
    val initialized: Boolean
    fun getDistanceFlow(): Flow<Float>
    suspend fun clear()
    suspend fun updateDistance(distance: Float)
}