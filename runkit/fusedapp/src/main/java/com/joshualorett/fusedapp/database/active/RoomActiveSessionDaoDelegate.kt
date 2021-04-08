package com.joshualorett.fusedapp.database.active

import com.joshualorett.fusedapp.database.toSession
import com.joshualorett.fusedapp.session.ActiveSessionDao
import com.joshualorett.runkit.session.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Delegates commands to the [RoomActiveSessionDao].
 * Created by Joshua on 3/10/2021.
 */
object RoomActiveSessionDaoDelegate: ActiveSessionDao {
    private lateinit var activeSessionDao: RoomActiveSessionDao
    var initialized = false

    fun init(activeSessionDao: RoomActiveSessionDao) {
        if (!initialized) {
            this.activeSessionDao = activeSessionDao
            initialized = true
        }
    }

    override fun getActiveSessionFlow(): Flow<Session> {
        return activeSessionDao.getActiveSession().map { sessionEntity ->
            sessionEntity?.toSession() ?: Session()
        }
    }
}