package com.joshualorett.fusedapp.location

import org.junit.Assert.*
import org.junit.Test

/**
 * Test [FusedLocationSettings].
 * Created by Joshua on 9/12/2020.
 */
class FusedLocationSettingsTest {
    @Test
    fun updateTimeDefaults() {
        assertEquals(FusedLocationSettings().updateInterval, 10000)
    }

    @Test
    fun fastestUpdateTimeDefaultsToHalf() {
        assertEquals(FusedLocationSettings().fastestUpdateInterval, 5000)
    }

    @Test
    fun fastestUpdateTimeHalvesProvidedUpdateTime() {
        assertEquals(FusedLocationSettings(20000).fastestUpdateInterval, 10000)
    }

    @Test
    fun shouldMatchUpdateTime() {
        assertEquals(FusedLocationSettings(100).updateInterval, 100)
    }

    @Test
    fun shouldMatchFastestUpdateTime() {
        assertEquals(FusedLocationSettings(1000, 500).fastestUpdateInterval, 500)
    }
}