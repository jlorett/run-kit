package com.joshualorett.fusedapp

import com.joshualorett.fusedapp.time.formatHoursMinutesSeconds
import org.junit.Test

import org.junit.Assert.*

/**
 * Tests [formatHoursMinutesSeconds].
 * Created by Joshua on 11/28/2020.
 */
class TimeFormatterKtTest {

    @Test
    fun lessThanASecond() {
        assertEquals("00:00:00", formatHoursMinutesSeconds(100.0))
    }

    @Test
    fun justSeconds() {
        assertEquals("00:00:01", formatHoursMinutesSeconds(1000.0))
    }

    @Test
    fun justMinutes() {
        assertEquals("00:01:00", formatHoursMinutesSeconds(1000.0*60))
    }

    @Test
    fun justHours() {
        assertEquals("01:00:00", formatHoursMinutesSeconds(1000.0*60*60))
    }

    @Test
    fun hourGreaterThanTwoDigits(){
        assertEquals("100:00:00", formatHoursMinutesSeconds(100000.0*60*60))
    }

    @Test
    fun timeCarriesToMinutes() {
        assertEquals("00:01:30", formatHoursMinutesSeconds(90.0*1000))
    }

    @Test
    fun timeCarriesToHours() {
        assertEquals("01:30:00", formatHoursMinutesSeconds(90.0*60*1000))
    }

    @Test
    fun timeCarriesToHoursAndMinutes() {
        assertEquals("01:30:03", formatHoursMinutesSeconds(90.0*60*1000 + 3*1000))
    }
}