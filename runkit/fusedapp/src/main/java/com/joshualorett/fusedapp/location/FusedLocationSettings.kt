package com.joshualorett.fusedapp.location

/**
 * Location settings for the [FusedLocationTracker].
 * @property updateInterval The desired interval for location updates in milliseconds.
 * @property fastestUpdateInterval The fastest interval for location updates in milliseconds.
 * Created by Joshua on 9/12/2020.
 */
data class FusedLocationSettings(val updateInterval: Long = 10000,
                                 val fastestUpdateInterval: Long = updateInterval/2)