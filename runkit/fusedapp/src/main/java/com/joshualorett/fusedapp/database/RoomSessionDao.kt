package com.joshualorett.fusedapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

/**
 * Data Access Object for Room Session Database.
 * Created by Joshua on 1/16/2021.
 */
@Dao
interface RoomSessionDao {
    @Transaction
    @Query("SELECT * FROM SessionEntity")
    fun getSessionWithLocations(): List<SessionWithLocations>
    @Insert
    fun createSession(session: SessionEntity): Long
    @Insert
    fun addLocation(location: LocationEntity): Long
}