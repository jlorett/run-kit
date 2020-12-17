package com.joshualorett.fusedapp.session

/**
 * Session.
 * Created by Joshua on 10/24/2020.
 */
data class Session(
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