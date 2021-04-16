package com.joshualorett.runkit.math.vo2max

/**
 * Estimate VO2 Max.
 * Created by Joshua on 4/15/2021.
 */

/***
 * Estimate VO2 Max in  mL/(kg*min) using the Heart Rate Ratio Method.
 * https://pubmed.ncbi.nlm.nih.gov/14624296/
 * @param hrResting Resting Heart Rate
 * @param hrMax Maximum Heart Rate
 */
fun vo2Max(hrResting: Int, hrMax: Int): Double {
    if(hrResting == 0) {
        return 0.0
    }
    return (hrMax / hrResting) * 15.3
}

/***
 * Estimate VO2 Max in mL/(kg*min) using the Cooper Test.
 * https://jamanetwork.com/journals/jama/article-abstract/337382
 * @param metersInTwelveMin Meters traveled in twelve minutes.
 */
fun vo2Max(metersInTwelveMin: Double): Double {
    return (metersInTwelveMin - 504.9) / 44.73
}




