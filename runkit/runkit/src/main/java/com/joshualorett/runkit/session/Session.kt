package com.joshualorett.runkit.session

import com.joshualorett.runkit.math.calories.kilocaloriesExpended
import com.joshualorett.runkit.math.calories.runningMet
import com.joshualorett.runkit.math.pace.millisecondsPerKilometer
import com.joshualorett.runkit.math.speed.milesPerHour
import java.util.*
import toIsoString

/**
 * Session.
 * Created by Joshua on 10/24/2020.
 */
data class Session(
    val id: Long = 0,
    val title: String? = null,
    val startTime: String = Date().toIsoString(),
    val endTime: String? = null,
    val elapsedTime: Long = 0,
    val distance: Double = 0.0,
    val state: State = State.STOPPED
) {
    enum class State {
        STARTED,
        STOPPED,
        PAUSED
    }

    fun averagePace(): Double {
        return millisecondsPerKilometer(elapsedTime, distance)
    }

    fun calories(kilograms: Double): Double {
        val mph = milesPerHour(distance, elapsedTime)
        val met = runningMet(mph)
        return kilocaloriesExpended(elapsedTime, kilograms, met)
    }
}
