package com.joshualorett.fusedapp.database.active

import androidx.room.Dao
import androidx.room.Query
import com.joshualorett.fusedapp.database.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Access data related to the active session.
 * Created by Joshua on 3/3/2021.
 */
@Dao
interface RoomActiveSessionDao {
    @Query("SELECT * FROM SessionEntity WHERE state=1 OR state=2 ORDER BY date DESC LIMIT 1")
    fun getActiveSession(): Flow<SessionEntity?>
}