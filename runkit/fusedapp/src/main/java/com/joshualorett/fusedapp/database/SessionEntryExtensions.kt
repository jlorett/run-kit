package com.joshualorett.fusedapp.database

import com.joshualorett.fusedapp.session.Session

/**
 * Extensions for [SessionEntity].
 * Created by Joshua on 1/31/2021.
 */

fun SessionEntity.toSession(): Session {
    return Session(id, title, startTime, endTime, elapsedTime, distance, state)
}