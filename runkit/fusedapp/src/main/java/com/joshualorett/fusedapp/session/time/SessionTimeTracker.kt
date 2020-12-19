package com.joshualorett.fusedapp.session.time

import android.os.SystemClock

/**
 * Track session time.
 * Created by Joshua on 12/13/2020.
 */
class SessionTimeTracker: TimeTracker {
    /***
     * uptimeMillis since boot when last started.
     */
    private var startedTime: Long = 0L
    /***
     * The amount of time that has passed in milliseconds since last stop. Note, this will
     * accumulate until [reset] is called.
     */
    private var elapsedTime: Long = 0L

    /***
     * Returns true if time tracker stopped, else false.
     */
    var stopped = true
        private set

    override fun start() {
        stopped = false
        startedTime = SystemClock.uptimeMillis()
    }

    override fun stop() {
        stopped = true
        elapsedTime += SystemClock.uptimeMillis() - startedTime
    }

    /***
     * Stop tracking time and reset elapsed time.
     */
    override fun reset() {
        stopped = true
        elapsedTime = 0L
    }

    override fun getElapsedTime(): Long {
        return if (stopped) {
            elapsedTime
        } else {
           elapsedTime + SystemClock.uptimeMillis() - startedTime
        }
    }
}