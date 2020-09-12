package com.joshualorett.fusedapp.location

/**
 * Location settings for the [LocationTracker].
 * Created by Joshua on 9/12/2020.
 */
data class LocationSettings(val updateIntervalMs: Long = 10000,
                            val fastestUpdaterIntervalMs: Long = updateIntervalMs/2)