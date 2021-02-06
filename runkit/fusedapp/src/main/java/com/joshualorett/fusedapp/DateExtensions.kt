package com.joshualorett.fusedapp

import java.text.SimpleDateFormat
import java.util.*

/**
 * Extensions for dates.
 * Created by Joshua on 1/31/2021.
 */
fun Date.toIsoString(): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        toInstant().toString()
    } else {
        SimpleDateFormat("YYYY-MM-DDTHH:MM:SS.SSSZ").format(this)
    }
}