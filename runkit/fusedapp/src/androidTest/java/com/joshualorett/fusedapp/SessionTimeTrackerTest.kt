package com.joshualorett.fusedapp

import com.joshualorett.fusedapp.time.ElapsedTimeTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests [ElapsedTimeTracker]
 * Created by Joshua on 12/13/2020.
 */
class SessionTimeTrackerTest {
    @Test
    fun elapsedZeroOnInit() = runBlocking {
        val timer = ElapsedTimeTracker()
        delay(100)
        val elapsedTime = timer.getElapsedTime()
        assertTrue(elapsedTime in 0..99)
    }

    @Test
    fun unStartedTimerStopsAtZero() = runBlocking {
        val timer = ElapsedTimeTracker()
        delay(100)
        timer.stop()
        val elapsedTime = timer.getElapsedTime()
        assertTrue(elapsedTime in 0..99)
    }

    @Test
    fun tracksMultipleStops() = runBlocking {
        val timer = ElapsedTimeTracker()
        timer.start()
        delay(100)
        timer.stop()
        timer.start()
        delay(200)
        timer.stop()
        val timeB = timer.getElapsedTime()
        assertTrue(timeB > 300)
    }

    @Test
    fun ignoresTimePassedWhenStopped() = runBlocking {
        val timer = ElapsedTimeTracker()
        timer.start()
        delay(100)
        timer.stop()
        delay(200)
        val timeB = timer.getElapsedTime()
        assertTrue(timeB in 100..199)
    }

    @Test
    fun calculatesAccurateElapsedTimeOnRestart() = runBlocking {
        val timer = ElapsedTimeTracker()
        timer.start()
        delay(100)
        timer.stop()
        delay(200)
        timer.start()
        val timeB = timer.getElapsedTime()
        assertTrue(timeB in 100..199)
    }

    @Test
    fun calculatesElapsedTimeWhileRunning() = runBlocking {
        val timer = ElapsedTimeTracker()
        timer.start()
        delay(100)
        val time = timer.getElapsedTime()
        assertTrue(time in 100..199)
    }

    @Test
    fun resetElapsedTimeToZero() = runBlocking {
        val timer = ElapsedTimeTracker()
        timer.start()
        delay(200)
        timer.reset()
        val time = timer.getElapsedTime()
        assertEquals(0, time)
    }

    @Test
    fun defaultsToStopped() {
        val timer = ElapsedTimeTracker()
        assertTrue(timer.stopped)
    }

    @Test
    fun stoppedOnStop() {
        val timer = ElapsedTimeTracker()
        timer.start()
        timer.stop()
        assertTrue(timer.stopped)
    }

    @Test
    fun stoppedOnReset() {
        val timer = ElapsedTimeTracker()
        timer.start()
        timer.reset()
        assertTrue(timer.stopped)
    }

    @Test
    fun startedOnStart() {
        val timer = ElapsedTimeTracker()
        timer.start()
        assertFalse(timer.stopped)
    }
}