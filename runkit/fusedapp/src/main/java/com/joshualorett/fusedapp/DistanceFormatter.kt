package com.joshualorett.fusedapp

/**
 * Formatted distance text.
 * Created by Joshua on 10/18/2020.
 */
fun formatDistance(distance: Float): String {
    return if (distance < 1000) {
        val roundedDistance = "%.2f".format(distance)
        "$roundedDistance m"
    } else {
        val roundedDistance = "%.2f".format(distance/1000)
        "$roundedDistance km"
    }
}