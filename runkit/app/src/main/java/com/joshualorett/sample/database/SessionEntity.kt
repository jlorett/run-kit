package com.joshualorett.sample.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.joshualorett.runkit.session.Session

/**
 * Room entity for a session.
 * Created by Joshua on 1/2/2021.
 */
@Entity
data class SessionEntity(
    @PrimaryKey
    val id: Long,
    val startTime: String,
    val endTime: String?,
    val title: String?,
    val distance: Double,
    val elapsedTime: Long,
)

fun SessionEntity.toSession(): Session {
    return Session(id, title, startTime, endTime, elapsedTime, distance)
}