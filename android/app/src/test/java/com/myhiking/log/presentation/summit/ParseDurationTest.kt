package com.myhiking.log.presentation.summit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParseDurationTest {

    @Test
    fun `blank is not entered`() = assertNull(parseDuration(" ", ""))

    @Test
    fun `hours and minutes`() {
        assertEquals(270, parseDuration("4", "30"))
        assertEquals(240, parseDuration("4", ""))
        assertEquals(45, parseDuration("", "45"))
    }

    @Test
    fun `out of range minutes is flagged`() = assertEquals(-1, parseDuration("1", "75"))

    @Test
    fun `fields round trip`() = assertEquals("4" to "30", durationFields(270))
}
