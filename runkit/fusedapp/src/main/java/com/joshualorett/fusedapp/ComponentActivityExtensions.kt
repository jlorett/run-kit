package com.joshualorett.fusedapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Extensions for [ComponentActivity]
 * Created by Joshua on 8/15/2020.
 */

fun ComponentActivity.hasPermission(permission: String): Boolean {
    return this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

fun ComponentActivity.hasFineLocationPermission(): Boolean {
    return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
}