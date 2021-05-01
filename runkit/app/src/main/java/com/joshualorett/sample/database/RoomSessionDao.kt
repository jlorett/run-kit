package com.joshualorett.sample.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Room Data access object for session.
 * Created by Joshua on 4/11/2021.
 */
@Dao
interface RoomSessionDao {
    @Query("SELECT * FROM SessionEntity")
    fun getSessions(): PagingSource<Int, SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<SessionEntity>)

    @Query("DELETE FROM SessionEntity")
    suspend fun clearAll()
}