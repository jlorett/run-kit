package com.joshualorett.fusedapp.session

import kotlinx.coroutines.flow.Flow

/**
 * Data access object for a session.
 * Created by Joshua on 9/27/2020.
 */
interface SessionDao {
    val initialized: Boolean
    fun getSessionFlow(): Flow<Session>
    suspend fun setSession(session: Session)
}