package com.joshualorett.fusedapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a session.
 * Created by Joshua on 1/2/2021.
 */
@Entity
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val date: String,
    val title: String?,
)