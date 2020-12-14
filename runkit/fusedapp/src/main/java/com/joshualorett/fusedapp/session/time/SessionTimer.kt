package com.joshualorett.fusedapp.session.time

import android.os.SystemClock

/**
 * Created by Joshua on 12/13/2020.
 */
class SessionTimer: Stopwatch {
    private var startedTime: Long = 0L
    private var stoppedTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var stopped = true

    override fun start() {
        stopped = false
        startedTime = SystemClock.uptimeMillis() + stoppedTime
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