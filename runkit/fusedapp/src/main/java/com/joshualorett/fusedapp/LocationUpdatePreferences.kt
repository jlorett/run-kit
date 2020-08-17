package com.joshualorett.fusedapp

import android.content.Context

/**
 * Stores preferences related to location updates.
 * Created by Joshua on 8/17/2020.
 */
object LocationUpdatePreferences {
    private const val preferencesKey = "FusedAppPreferences"
    const val requestLocationUpdatesKey = "requesting_location_updates"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun requestingLocationUpdates(context: Context): Boolean {
        return context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE).getBoolean(requestLocationUpdatesKey, false) ?: false
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean) {
        context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(requestLocationUpdatesKey, requestingLocationUpdates)
            .apply()
    }
}