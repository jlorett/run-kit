package com.joshualorett.fusedapp.database

import androidx.room.Embedded
import androidx.room.Relation
import com.joshualorett.fusedapp.database.location.LocationEntity

/**
 * A session with its list of locations.
 * Created by Joshua on 1/2/2021.
 */
data class SessionWithLocations(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val locations: List<LocationEntity>,
)
