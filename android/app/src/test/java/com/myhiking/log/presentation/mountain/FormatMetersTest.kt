package com.myhiking.log.presentation.mountain

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatMetersTest {
    @Test
    fun `drops decimals and groups thousands`() {
        assertEquals("835m", formatMeters(835.6))
        assertEquals("999m", formatMeters(999.7))
        assertEquals("1,947m", formatMeters(1947.3))
    }
}
