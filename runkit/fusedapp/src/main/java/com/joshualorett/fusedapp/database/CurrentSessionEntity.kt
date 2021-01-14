package com.joshualorett.fusedapp.database

import androidx.room.Entity
import com.joshualorett.fusedapp.session.Session

/**
 * Entity for current session.
 * Created by Joshua on 1/13/2021.
 */
@Entity
data class CurrentSessionEntity(
    val date: String,
    val title: String?,
    val elapsedTime: Long,
    val distance: Float,
    val state: Session.State
)
