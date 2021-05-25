package com.joshualorett.fusedapp.database

import androidx.paging.PagingSource
import androidx.room.*
import com.joshualorett.runkit.session.Session
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Session Database.
 * Created by Joshua on 1/16/2021.
 */
@Dao
interface RoomSessionDao {
    @Transaction
    @Query("SELECT * FROM SessionEntity WHERE state=0")
    fun getSessionWithLocations(): Flow<List<SessionWithLocations>>
    @Query("SELECT * FROM SessionEntity WHERE state=0")
    fun getSessions(): Flow<List<Session>>
    @Query("SELECT * FROM SessionEntity WHERE state=0 ORDER BY id DESC")
    fun getPagedSessions(): PagingSource<Int, SessionEntity>
    @Insert
    suspend fun createSession(session: SessionEntity): Long
    @Query("UPDATE SessionEntity SET distance=:distance WHERE id=:id")
    suspend fun updateSessionDistance(id: Long, distance: Double)
    @Query("UPDATE SessionEntity SET elapsedTime=:elapsedTime WHERE id=:id")
    suspend fun updateSessionElapsedTime(id: Long, elapsedTime: Long)
    @Query("UPDATE SessionEntity SET state=:sessionState WHERE id=:id")
    suspend fun updateSessionState(id: Long, sessionState: Session.State)
    @Query("UPDATE SessionEntity SET endTime=:endTime WHERE id=:id")
    suspend fun updateSessionEndTime(id: Long, endTime: String)
}