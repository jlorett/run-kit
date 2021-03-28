package com.joshualorett.fusedapp.math.speed

/**
 * Calculate Speed.
 * Created by Joshua on 3/28/2021.
 */

fun kilometersPerHour(meters: Double, milliseconds: Long): Double {
    val oneKm = 1000.0
    val oneHour = 3600000.0
    val hours = milliseconds/oneHour
    val kilometers = meters/oneKm
    return kilometers/hours
}