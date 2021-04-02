package com.joshualorett.runkit.time

/**
 * Track time.
 * Created by Joshua on 12/13/2020.
 */
interface TimeTracker {
    fun start()
    fun start(elapsedTime: Long)
    fun stop()
    fun reset()
    fun getElapsedTime(): Long
}