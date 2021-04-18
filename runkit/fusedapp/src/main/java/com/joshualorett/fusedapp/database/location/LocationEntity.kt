package com.joshualorett.fusedapp.database.location

import androidx.room.Entity
import com.joshualorett.fusedapp.toIsoString
import com.joshualorett.runkit.location.Location
import java.util.*

/**
 * Room entity for a location.
 * Created by Joshua on 1/2/2021.
 */
@Entity(primaryKeys = ["sessionId", "date"])
data class LocationEntity(
    val sessionId: Long,
    val date: String,
    val latitude: Double,
    val longitude: Double) {

    constructor(sessionId: Long, location: Location) : this(sessionId,
        Date(location.timeMs).toIsoString(),
        location.latitude,
        location.longitude)
}