package com.joshualorett.fusedapp

import com.joshualorett.fusedapp.session.time.SessionTimeTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests [SessionTimeTracker]
 * Created by Joshua on 12/13/2020.
 */
class SessionTimeTrackerTest {

    @Test
    fun tracksMultipleStops() = runBlocking {
        val timer = SessionTimeTracker()
        timer.start()
        delay(200)
        timer.stop()
        timer.start()
        delay(300)
        timer.stop()
        val timeB = timer.getElapsedTime()
        assertTrue(timeB > 500)
    }

    @Test
    fun ignoresTimePassedWhenStopped() = runBlocking {
        val timer = SessionTimeTracker()
        timer.start()
        delay(200)
        timer.stop()
        delay(300)
        val timeB = timer.getElapsedTime()
        assertTrue(timeB in 201..299)
    }

    @Test
    fun calculatesAccurateElapsedTimeOnRestart() = runBlocking {
        val timer = SessionTimeTracker()
        timer.start()
        delay(200)
        timer.stop()
        delay(300)
        timer.start()
        val timeB = timer.getElapsedTime()
        assertTrue(timeB in 201..299)
    }

    @Test
    fun calculatesElapsedTimeWhileRunning() = runBlocking {
        val timer = SessionTimeTracker()
        timer.start()
        delay(200)
        val time = timer.getElapsedTime()
        assertTrue(time in 201..299)
    }

    @Test
    fun resetElapsedTimeToZero() = runBlocking {
        val timer = SessionTimeTracker()
        timer.start()
        delay(200)
        timer.reset()
        val time = timer.getElapsedTime()
        assertEquals(0, time)
    }

    @Test
    fun defaultsToStopped() {
        val timer = SessionTimeTracker()
        assertTrue(timer.stopped)
    }

    @Test
    fun stoppedOnStop() {
        val timer = SessionTimeTracker()
        timer.start()
        timer.stop()
        assertTrue(timer.stopped)
    }

    @Test
    fun stoppedOnReset() {
        val timer = SessionTimeTracker()
        timer.start()
        timer.reset()
        assertTrue(timer.stopped)
    }

    @Test
    fun startedOnStart() {
        val timer = SessionTimeTracker()
        timer.start()
        assertFalse(timer.stopped)
    }
}