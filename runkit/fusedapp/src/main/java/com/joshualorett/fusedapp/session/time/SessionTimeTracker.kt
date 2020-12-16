package com.joshualorett.fusedapp.session.time

import android.os.SystemClock

/**
 * Track session time.
 * Created by Joshua on 12/13/2020.
 */
class SessionTimeTracker: TimeTracker {
    private var startedTime: Long = 0L
    private var stoppedTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var stopped = true

    override fun start() {
        stopped = false
        startedTime = SystemClock.uptimeMillis()
    }

    override fun stop() {
        stopped = true
        stoppedTime = startedTime - SystemClock.uptimeMillis()
        elapsedTime += SystemClock.uptimeMillis() - startedTime
    }

    override fun reset() {
        stopped = true
        stoppedTime = 0L
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