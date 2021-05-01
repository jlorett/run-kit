package com.joshualorett.runkit.time

import android.os.SystemClock

/**
 * Track time.
 * Created by Joshua on 12/13/2020.
 */
class TimeTracker {
    private var startedTime: Long = 0L
    private var elapsedTime: Long = 0L
    var stopped = true
        private set

    fun start() {
        stopped = false
        startedTime = SystemClock.uptimeMillis()
    }

    fun start(elapsedTime: Long) {
        this.elapsedTime = elapsedTime
        stopped = false
        startedTime = SystemClock.uptimeMillis()
    }

    fun stop() {
        stopped = true
        if (startedTime == 0L) {
            startedTime = SystemClock.uptimeMillis()
        }
        elapsedTime += SystemClock.uptimeMillis() - startedTime
    }

    fun reset() {
        stopped = true
        elapsedTime = 0L
    }

    fun getElapsedTime(): Long {
        val now = SystemClock.uptimeMillis()
        return if (stopped) {
            elapsedTime
        } else {
            elapsedTime + now - startedTime
        }
    }
}
