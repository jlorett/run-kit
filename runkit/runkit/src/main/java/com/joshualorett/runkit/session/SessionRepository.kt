package com.joshualorett.runkit.session

import kotlinx.coroutines.flow.Flow

/**
 * The main point of access to running sessions.
 * Created by Joshua on 4/2/2021.
 */
class SessionRepository(sessionDao: SessionDao) {
    val sessions: Flow<List<Session>> = sessionDao.getSessions()
}
