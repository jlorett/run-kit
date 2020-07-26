package com.joshualorett.runkit.permission

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat

/**
 * Requests location permissions.
 * Created by Joshua on 7/18/2020.
 */
object LocationPermissionRequester {
    fun requestCoarseLocationPermission(context: Context): Int {
        return requestPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun requestFineLocationPermission(context: Context): Int {
        return requestPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    }

    private fun requestPermission(context: Context, permission: String): Int {
        return ContextCompat.checkSelfPermission(context, permission)
    }
}