package com.joshualorett.fusedapp

import android.location.Location

/**
 * Extensions for [Location]
 * Created by Joshua on 8/17/2020.
 */

fun Location.getLocationText(): String {
    return "(${this.latitude}, ${this.longitude})"
}