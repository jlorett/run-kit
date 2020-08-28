package com.joshualorett.fusedapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Extensions for [ComponentActivity]
 * Created by Joshua on 8/15/2020.
 */

/**
 * Runs a task if permission is granted otherwise, fallback if permission is not granted.
 */
@SuppressLint("MissingPermission")
fun ComponentActivity.withPermission(permission: String, run: () -> Unit, fallback: () -> Unit) {
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            run()
        } else {
            fallback()
        }
    }.launch(permission)
}

fun ComponentActivity.hasPermission(permission: String): Boolean {
    return this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}