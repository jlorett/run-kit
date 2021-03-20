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
fun millisecondsPerKilometer(meters: Double, milliseconds: Long): Double {
    if(meters == 0.0) {
        return 0.0
    }
    val oneKm = 1000
    if(meters < oneKm) {
        return milliseconds + millisecondsTillKilometer(meters, milliseconds)
    }
    val kilometers = meters/oneKm
    return milliseconds/kilometers
}

/***
 * Returns how much time in milliseconds left until a kilometer is reached at the current pace.
 * @param meters distance in meters
 * @param milliseconds time in milliseconds
 */
private fun millisecondsTillKilometer(meters: Double, milliseconds: Long): Double {
    val oneKm = 1000
    return if(meters < oneKm) {
        val paceMsPerM = milliseconds/meters
        val metersTillKm = oneKm - meters
        return paceMsPerM * metersTillKm
    } else 0.0
}