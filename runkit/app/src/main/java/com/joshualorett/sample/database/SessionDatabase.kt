package com.joshualorett.sample.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Store session and its locations.
 * Created by Joshua on 1/17/2021.
 */
@Database(entities = [SessionEntity::class], version = 1, exportSchema = false)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): RoomSessionDao

    companion object {
        val DB = "session.db"

        @Volatile
        private var INSTANCE: SessionDatabase? = null

        fun getInstance(context: Context): SessionDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, SessionDatabase::class.java, DB)
                .addCallback(object: RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        MainScope().launch(Dispatchers.Default) {
                            fillInDb(context.applicationContext)
                        }
                    }
                })
                .build()

        @SuppressLint("NewApi")
        private suspend fun fillInDb(context: Context) {
            val data = mutableListOf<SessionEntity>()
            val words = listOf("Morning", "Night", "Afternoon", "Long", "Short", "Tempo")
            for(i in 1..100) {
                data.add(
                    SessionEntity(id = i.toLong(), title = "${words.random()} Run", startTime = LocalDate.now().toString(),
                    endTime = LocalDate.now().toString(), distance = 10000.0, elapsedTime = 60*60*60*1000))
            }
            getInstance(context).sessionDao().insertAll(
                data
            )
        }
    }
}
