package com.joshualorett.fusedapp.session.time

/**
 * Track time.
 * Created by Joshua on 12/13/2020.
 */
interface Stopwatch {
    fun start()
    fun stop()
    fun reset()
    fun getElapsedTime(): Long
}