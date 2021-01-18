package com.joshualorett.fusedapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a location.
 * Created by Joshua on 1/2/2021.
 */
@Entity
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val sessionId: Int,
    val date: String,
    val latitude: Double,
    val longitude: Double)