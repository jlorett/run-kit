package com.joshualorett.fusedapp

import com.joshualorett.fusedapp.time.formatHourMinuteSeconds
import org.junit.Test

import org.junit.Assert.*

/**
 * Tests [formatHourMinuteSeconds].
 * Created by Joshua on 11/28/2020.
 */
class TimeFormatterKtTest {

    @Test
    fun lessThanASecond() {
        assertEquals("00:00:00", formatHourMinuteSeconds(100))
    }

    @Test
    fun justSeconds() {
        assertEquals("00:00:01", formatHourMinuteSeconds(1000))
    }

    @Test
    fun justMinutes() {
        assertEquals("00:01:00", formatHourMinuteSeconds(1000*60))
    }

    @Test
    fun justHours() {
        assertEquals("01:00:00", formatHourMinuteSeconds(1000*60*60))
    }

    @Test
    fun hourGreaterThanTwoDigits(){
        assertEquals("100:00:00", formatHourMinuteSeconds(100000*60*60))
    }

    @Test
    fun timeCarriesToMinutes() {
        assertEquals("00:01:30", formatHourMinuteSeconds(90*1000))
    }

    @Test
    fun timeCarriesToHours() {
        assertEquals("01:30:00", formatHourMinuteSeconds(90*60*1000))
    }

    @Test
    fun timeCarriesToHoursAndMinutes() {
        assertEquals("01:30:03", formatHourMinuteSeconds(90*60*1000 + 3*1000))
    }
}