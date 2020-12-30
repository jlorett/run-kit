package com.joshualorett.fusedapp.session

/**
 * Interact with a location tracking session.
 * Created by Joshua on 11/14/2020.
 */
interface SessionService {
    fun start()
    fun stop()
    fun pause()
}