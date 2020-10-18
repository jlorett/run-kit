package com.joshualorett.fusedapp

import android.app.ActivityManager
import android.content.Context

/**
 * Service Utilities.
 * Created by Joshua on 10/17/2020.
 */
fun <T> serviceIsRunningInForeground(serviceClass: Class<T>, context: Context): Boolean {
    val activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for(service: ActivityManager.RunningServiceInfo in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        if(serviceClass.name == service.service.className) {
            if(service.foreground) {
                return true
            }
        }
    }
    return false
}