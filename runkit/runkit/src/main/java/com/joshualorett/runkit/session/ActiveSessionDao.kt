package com.joshualorett.runkit.session

import kotlinx.coroutines.flow.Flow

/**
 * Data access object for a session.
 * Created by Joshua on 3/10/2021.
 */
interface ActiveSessionDao {
    fun getActiveSessionFlow(): Flow<Session>
}
