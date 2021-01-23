package com.joshualorett.fusedapp.database

import androidx.room.*
import com.joshualorett.fusedapp.session.Session
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Session Database.
 * Created by Joshua on 1/16/2021.
 */
@Dao
interface RoomSessionDao {
    @Transaction
    @Query("SELECT * FROM SessionEntity")
    fun getSessionWithLocations(): Flow<List<SessionWithLocations>>
    @Query("SELECT distance FROM SessionEntity WHERE id=:id")
    fun getSessionDistance(id: Long): Flow<Float>
    @Query("SELECT elapsedTime FROM SessionEntity WHERE id=:id")
    fun getSessionElapsedTime(id: Long): Flow<Long>
    @Query("SELECT state FROM SessionEntity WHERE id=:id")
    fun getSessionState(id: Long): Flow<Session.State>
    @Query("SELECT * FROM SessionEntity WHERE state=1 OR state=2 ORDER BY date DESC LIMIT 1")
    fun getCurrentSession(): Flow<SessionEntity?>
    @Insert
    fun createSession(session: SessionEntity): Long
    @Insert
    fun addLocation(location: LocationEntity): Long
    @Query("UPDATE SessionEntity SET distance=:distance WHERE id=:id")
    fun updateSessionDistance(id: Long, distance: Float)
    @Query("UPDATE SessionEntity SET elapsedTime=:elapsedTime WHERE id=:id")
    fun updateSessionElapsedTime(id: Long, elapsedTime: Long)
    @Query("UPDATE SessionEntity SET state=:sessionState WHERE id=:id")
    fun updateSessionState(id: Long, sessionState: Session.State)
}