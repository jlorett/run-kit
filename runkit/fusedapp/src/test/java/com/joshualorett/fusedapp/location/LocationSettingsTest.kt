package com.joshualorett.fusedapp.location

import org.junit.Assert.*
import org.junit.Test

/**
 * Test [LocationSettings].
 * Created by Joshua on 9/12/2020.
 */
class LocationSettingsTest {
    @Test
    fun updateTimeDefaults() {
        assertEquals(LocationSettings().updateIntervalMs, 10000)
    }

    @Test
    fun fastestUpdateTimeDefaultsToHalf() {
        assertEquals(LocationSettings().fastestUpdaterIntervalMs, 5000)
    }

    @Test
    fun fastestUpdateTimeHalvesProvidedUpdateTime() {
        assertEquals(LocationSettings(20000).fastestUpdaterIntervalMs, 10000)
    }

    @Test
    fun shouldMatchUpdateTime() {
        assertEquals(LocationSettings(100).updateIntervalMs, 100)
    }

    @Test
    fun shouldMatchFastestUpdateTime() {
        assertEquals(LocationSettings(1000, 500).fastestUpdaterIntervalMs, 500)
    }
}