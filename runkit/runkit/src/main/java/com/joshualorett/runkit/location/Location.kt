package com.joshualorett.runkit.location

import com.joshualorett.runkit.math.distance.meters

/**
 * A geographic location.
 * Created by Joshua on 4/14/2021.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val timeMs: Long
) {
    fun distanceTo(destination: Location): Double {
        return meters(latitude, longitude, destination.latitude, destination.longitude)
    }
}
