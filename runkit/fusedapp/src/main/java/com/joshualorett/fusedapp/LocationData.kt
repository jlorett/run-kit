package com.joshualorett.fusedapp

import android.location.Location

/**
 * Location data from [FusedLocationListener].
 * Created by Joshua on 8/19/2020.
 */
sealed class LocationData {
    data class Success(val location: Location): LocationData()
    sealed class Error(val exception: Exception?): LocationData() {
        data class PermissionError(val permissionException: Exception?): Error(permissionException)
        object MissingLocation: Error(null)
    }
}