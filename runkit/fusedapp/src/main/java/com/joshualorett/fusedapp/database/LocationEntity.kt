package com.joshualorett.fusedapp.database

import androidx.room.PrimaryKey

/**
 * Room entity for a location.
 * Created by Joshua on 1/2/2021.
 */
data class LocationEntity(@PrimaryKey val id: Long,
                          val sessionId: Long,
                          val date: String,
                          val latitude: Float,
                          val longitude: Float)
