package com.joshualorett.runkit.math.speed

/**
 * Calculate Speed.
 * Created by Joshua on 3/28/2021.
 */

fun kilometersPerHour(meters: Double, milliseconds: Long): Double {
    if (milliseconds == 0L) {
        return 0.0
    }
    val oneKm = 1000.0
    val oneHour = 3600000.0
    val hours = milliseconds / oneHour
    val kilometers = meters / oneKm
    return kilometers / hours
}

fun milesPerHour(meters: Double, milliseconds: Long): Double {
    if (milliseconds == 0L) {
        return 0.0
    }
    val oneMile = 1609.344
    val oneHour = 3600000.0
    val hours = milliseconds / oneHour
    val miles = meters / oneMile
    return miles / hours
}
