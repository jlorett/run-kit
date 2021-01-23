package com.joshualorett.fusedapp.database

import androidx.room.TypeConverter
import com.joshualorett.fusedapp.session.Session

/**
 * Converts [Session.State] to readable format for room
 * Created by Joshua on 1/22/2021.
 */
class SessionStateTypeConverter {
    @TypeConverter
    fun fromSessionState(value: Session.State): Int {
        return when(value) {
            Session.State.STOPPED -> 0
            Session.State.STARTED -> 1
            Session.State.PAUSED -> 2
        }
    }

    @TypeConverter
    fun toSessionState(value: Int): Session.State {
        return when(value) {
            0 -> Session.State.STOPPED
            1 -> Session.State.STARTED
            2 -> Session.State.PAUSED
            else -> throw IllegalArgumentException("Session.State value must be 0, 1, or 2.")
        }
    }
}