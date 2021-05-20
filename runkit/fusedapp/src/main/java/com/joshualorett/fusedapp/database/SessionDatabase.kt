package com.joshualorett.fusedapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.joshualorett.fusedapp.database.active.RoomActiveSessionDao
import com.joshualorett.fusedapp.database.location.LocationEntity
import com.joshualorett.fusedapp.database.location.RoomLocationDao

/**
 * Store session and its locations.
 * Created by Joshua on 1/17/2021.
 */
@Database(entities = [SessionEntity::class, LocationEntity::class], version = 1)
@TypeConverters(SessionStateTypeConverter::class)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): RoomSessionDao
    abstract fun activeSessionDao(): RoomActiveSessionDao
    abstract fun locationDao(): RoomLocationDao
}

object SessionDatabaseFactory {
    private lateinit var db: SessionDatabase
    fun getInstance(context: Context): SessionDatabase {
        if(!::db.isInitialized) {
            db = Room.databaseBuilder(
                context.applicationContext,
                SessionDatabase::class.java, "session"
            ).build()
        }
        return db
    }
}
