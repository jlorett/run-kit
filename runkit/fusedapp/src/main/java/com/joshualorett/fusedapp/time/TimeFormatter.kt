package com.joshualorett.fusedapp.time

/**
 * Format time in milliseconds to hh:mm:ss
 */
fun formatHoursMinutesSeconds(timeInMs: Double): String {
    val oneHourMs = 60*60*1000
    val oneMinuteMs = 60*1000
    val oneSecondMs = 1000
    val hours = "%02d".format((timeInMs/oneHourMs).toInt())
    val minutes = "%02d".format((timeInMs/oneMinuteMs%60).toInt())
    val seconds = "%02d".format((timeInMs/oneSecondMs%60).toInt())
    return "$hours:$minutes:$seconds"
}

/**
 * Format time in milliseconds to mm:ss.
 */
fun formatMinutesSeconds(timeInMs: Double): String {
    val oneMinuteMs = 60*1000
    val oneSecondMs = 1000
    val minutes = "%02d".format((timeInMs/oneMinuteMs).toInt())
    val seconds = "%02d".format((timeInMs/oneSecondMs%60).toInt())
    return "$minutes:$seconds"
}