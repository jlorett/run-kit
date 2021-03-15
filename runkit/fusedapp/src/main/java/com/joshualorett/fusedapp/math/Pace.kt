package com.joshualorett.fusedapp.math

/**
 * Calculate Pace.
 * Created by Joshua on 3/14/2021.
 */

/***
 * Returns pace in milliseconds per kilometer.
 * @param meters distance in meters
 * @param milliseconds time in milliseconds
 */
fun millisecondsPerKilometer(meters: Long, milliseconds: Long): Long {
    if(meters == 0L) {
        return 0L
    }
    val oneKm = 1000
    if(meters < oneKm) {
        val timeTillKm = millisecondsTillKilometer(meters, milliseconds)
        return milliseconds + timeTillKm
    }
    val kilometers = meters/oneKm
    return milliseconds/kilometers
}

/***
 * Returns how much time in milliseconds left until a kilometer is reached at the current pace.
 * @param meters distance in meters
 * @param milliseconds time in milliseconds
 */
private fun millisecondsTillKilometer(meters: Long, milliseconds: Long): Long {
    val oneKm = 1000
    return if(meters < oneKm) {
        val paceMsPerM = milliseconds/meters
        val metersTillKm = oneKm - meters
        return paceMsPerM * metersTillKm
    } else 0
}