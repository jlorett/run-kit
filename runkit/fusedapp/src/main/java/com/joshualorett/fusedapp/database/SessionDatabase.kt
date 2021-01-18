package com.joshualorett.fusedapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Store session and its locations.
 * Created by Joshua on 1/17/2021.
 */
@Database(entities = [SessionEntity::class, LocationEntity::class], version = 1)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): RoomSessionDao
}
