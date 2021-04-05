package com.joshualorett.runkit.math.pace

import org.junit.Test

import org.junit.Assert.*

/**
 * Test [millisecondsPerKilometer].
 * Created by Joshua on 4/4/2021.
 */
class PaceKtTest {
    private val delta = 1e-2

    @Test
    fun millisecondsPerKilometer() {
        val pace = millisecondsPerKilometer(7299000, 42195.0)
        assertEquals(172982.580, pace, delta)
    }
}