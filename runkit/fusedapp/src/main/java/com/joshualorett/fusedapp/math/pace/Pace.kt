package com.joshualorett.fusedapp.math.pace

/**
 * Calculate Pace.
 * Created by Joshua on 3/14/2021.
 */

/***
 * Returns pace in milliseconds per kilometer.
 * @param meters distance in meters
 * @param milliseconds time in milliseconds
 */
fun millisecondsPerKilometer(milliseconds: Long, meters: Double): Double {
    val oneKm = 1000.0
    val kilometers = meters/oneKm
    return milliseconds/kilometers
}