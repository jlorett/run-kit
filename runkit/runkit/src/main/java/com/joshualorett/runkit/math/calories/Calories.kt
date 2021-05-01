package com.joshualorett.runkit.math.calories

/**
 * Calculate Calories Burned.
 * Created by Joshua on 3/20/2021.
 */

/***
 * Returns calories expended for a given amount of time in kilocalories.
 * @param milliseconds time in milliseconds
 * @param kilograms weight in kilograms
 * @param met Metabolic equivalent of task (MET) for your activity.
 * See [The Compendium of Physical Activities](https://sites.google.com/site/compendiumofphysicalactivities/home)
 * for more information on how an MET is calculated.
 * @param oneMet The value of 1 MET. This may differ depending on an individual's resting metabolic rate. See [Metabolic equivalent: one size does not fit all](https://pubmed.ncbi.nlm.nih.gov/15831804/)
 * This defaults to 3.5 as suggested in the [Compendium of physical activities: classification of energy costs of human physical activities](https://pubmed.ncbi.nlm.nih.gov/8292105/)
 */
fun kilocaloriesExpended(milliseconds: Long, kilograms: Double, met: Double, oneMet: Double = 3.5): Double {
    val minutes = milliseconds / 1000.0 / 60
    return minutes * met * oneMet * kilograms / 200
}
