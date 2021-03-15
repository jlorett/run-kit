package com.joshualorett.fusedapp.session

import com.joshualorett.fusedapp.toIsoString
import java.util.*

/**
 * Session.
 * Created by Joshua on 10/24/2020.
 */
data class Session(
    val id: Long = 0,
    val title: String? = null,
    val startTime: String = Date().toIsoString(),
    val endTime: String? = null,
    val elapsedTime: Long = 0,
    val distance: Float = 0F,
    val state: State = State.STOPPED
) {
    enum class State {
        STARTED,
        STOPPED,
        PAUSED
    }
}