package com.joshualorett.fusedapp.math.pace

import kotlin.math.roundToLong

/**
 * Calculate Pace.
 * Created by Joshua on 3/14/2021.
 */

/***
 * Returns pace in milliseconds per kilometer.
 * @param meters distance in meters
 * @param milliseconds time in milliseconds
 */
fun millisecondsPerKilometer(meters: Float, milliseconds: Long): Long {
    if(meters == 0F) {
        return 0L
    }
    val oneKm = 1000
    if(meters < oneKm) {
        val timeTillKm = millisecondsTillKilometer(meters, milliseconds)
        return milliseconds + timeTillKm
    }
    val kilometers = meters/oneKm
    return (milliseconds/kilometers).roundToLong()
}

/***
 * Returns how much time in milliseconds left until a kilometer is reached at the current pace.
 * @param meters distance in meters
 * @param milliseconds time in milliseconds
 */
private fun millisecondsTillKilometer(meters: Float, milliseconds: Long): Long {
    val oneKm = 1000
    return if(meters < oneKm) {
        val paceMsPerM = milliseconds/meters
        val metersTillKm = oneKm - meters
        return (paceMsPerM * metersTillKm).roundToLong()
    } else 0
}