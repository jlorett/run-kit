package com.joshualorett.fusedapp

import com.joshualorett.fusedapp.session.time.SessionTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Tests [SessionTimer]
 * Created by Joshua on 12/13/2020.
 */
class SessionTimerTest {

    @Test
    fun tracksMultipleStops() {
        runBlocking {
            val timer = SessionTimer()
            timer.start()
            delay(200)
            timer.stop()
            timer.start()
            delay(300)
            timer.stop()
            val timeB = timer.getElapsedTime()
            assertTrue(timeB > 500)
        }
    }

    @Test
    fun ignoresTimePassedWhenStopped() {
        runBlocking {
            val timer = SessionTimer()
            timer.start()
            delay(200)
            timer.stop()
            delay(300)
            val timeB = timer.getElapsedTime()
            assertTrue(timeB in 201..299)
        }
    }

    @Test
    fun calculatesAccurateElapsedTimeOnRestart() {
        runBlocking {
            val timer = SessionTimer()
            timer.start()
            delay(200)
            timer.stop()
            delay(300)
            timer.start()
            val timeB = timer.getElapsedTime()
            assertTrue(timeB in 201..299)
        }
    }

    @Test
    fun calculatesElapsedTimeWhileRunning() {
        runBlocking {
            val timer = SessionTimer()
            timer.start()
            delay(200)
            val time = timer.getElapsedTime()
            assertTrue(time in 201..299)
        }
    }
}