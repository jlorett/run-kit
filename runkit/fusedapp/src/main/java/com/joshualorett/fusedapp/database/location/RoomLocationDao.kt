package com.joshualorett.fusedapp.database.location

import androidx.room.Dao
import androidx.room.Insert

/**
 * Access location related data.
 * Created by Joshua on 3/3/2021.
 */

@Dao
interface RoomLocationDao {
    @Insert
    suspend fun addLocation(location: LocationEntity): Long
}