package com.joshualorett.fusedapp.database

import androidx.room.Entity

/**
 * Room entity for a location.
 * Created by Joshua on 1/2/2021.
 */
@Entity(primaryKeys = ["sessionId", "date"])
data class LocationEntity(
    val sessionId: Long,
    val date: String,
    val latitude: Double,
    val longitude: Double)