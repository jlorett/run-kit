package com.joshualorett.fusedapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.joshualorett.fusedapp.session.Session

/**
 * Room entity for a session.
 * Created by Joshua on 1/2/2021.
 */
@Entity
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val startTime: String,
    val endTime: String?,
    val title: String?,
    val distance: Float,
    val elapsedTime: Long,
    val state: Session.State
)