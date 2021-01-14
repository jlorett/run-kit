package com.joshualorett.fusedapp.database

import androidx.room.TypeConverter
import com.joshualorett.fusedapp.session.Session

/**
 * Converts Session.State into value readable by the database.
 * Created by Joshua on 1/13/2021.
 */
class SessionStateConverter {
    @TypeConverter
    fun toSessionState(value: String): Session.State = enumValueOf(value)

    @TypeConverter
    fun fromSessionState(state: Session.State): String = state.name
}