package com.joshualorett.runkit.math.calories

/**
 * Metabolic equivalent of task (MET) for an activity.
 * See [The Compendium of Physical Activities](https://sites.google.com/site/compendiumofphysicalactivities/home)
 * for more information on how METs are calculated.
 * Created by Joshua on 3/20/2021.
 */

const val metFourMph = 6.0
const val metFiveMph = 8.3
const val metSixMph = 9.8
const val metSevenMph = 11.0
const val metEightMph = 11.8
const val metNineMph = 12.8
const val metTenMph = 14.5
const val metElevenMph = 16.0
const val metTwelveMph = 19.0
const val metThirteenMph = 19.8
const val metFourteenMph = 23.0
const val metMarathon = 13.3
const val metRunning = 8.0

/**
 * Returns MET based on speed between [4 mph, 14 mph].
 */
fun runningMet(milesPerHour: Double): Double {
    return when {
        milesPerHour < 5.0 -> metFourMph
        milesPerHour < 6.0 -> metFiveMph
        milesPerHour < 7.0 -> metSixMph
        milesPerHour < 8.0 -> metSevenMph
        milesPerHour < 9.0 -> metEightMph
        milesPerHour < 10.0 -> metNineMph
        milesPerHour < 11.0 -> metTenMph
        milesPerHour < 12.0 -> metElevenMph
        milesPerHour < 13.0 -> metTwelveMph
        milesPerHour < 14.0 -> metThirteenMph
        else -> metFourteenMph
    }
}