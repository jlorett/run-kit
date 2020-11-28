package com.joshualorett.fusedapp

/**
 * Format time in milliseconds to hours, minutes, seconds.
 * Created by Joshua on 11/28/2020.
 */
fun formatHourMinuteSeconds(timeInMs: Long): String {
    val oneHourMs = 60*60*1000
    val oneMinuteMs = 60*1000
    val oneSecondMs = 1000
    val hours = String.format("%02d", timeInMs/oneHourMs)
    val minutes = String.format("%02d", timeInMs/oneMinuteMs%60)
    val seconds = String.format("%02d", timeInMs/oneSecondMs%60)
    return "$hours:$minutes:$seconds"
}