package com.joshualorett.fusedapp.time

import android.os.SystemClock

/**
 * Track session time.
 * Created by Joshua on 12/13/2020.
 */
class ElapsedTimeTracker: TimeTracker {
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

    override fun start(elapsedTime: Long) {
        this.elapsedTime = elapsedTime
        stopped = false
        startedTime = SystemClock.uptimeMillis()
    }

    override fun stop() {
        stopped = true
        if(startedTime == 0L) {
            startedTime = SystemClock.uptimeMillis()
        }
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
        val now = SystemClock.uptimeMillis()
        return if (stopped) {
            elapsedTime
        } else {
           elapsedTime + now - startedTime
        }
    }
}