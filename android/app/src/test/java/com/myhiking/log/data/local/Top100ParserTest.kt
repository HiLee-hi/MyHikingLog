package com.myhiking.log.data.local

import com.myhiking.log.domain.model.Difficulty
import com.myhiking.log.domain.model.RegionGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

// reads the real asset -> catches a bad convert_top100.py run
class Top100ParserTest {

    private val mountains = parseTop100(File("src/main/assets/top100_mountains.json").readText())

    @Test
    fun `asset holds 100 unique mountains`() {
        assertEquals(100, mountains.size)
        assertEquals(100, mountains.map { it.id }.toSet().size)
        assertTrue(mountains.all { it.isTop100 })
    }

    @Test
    fun `every mountain has a name, height, position and a province`() {
        mountains.forEach {
            assertTrue(it.name, it.name.isNotBlank())
            assertTrue(it.name, (it.heightMeters ?: 0.0) > 0)
            assertTrue(it.name, it.latitude!! in 33.0..39.0 && it.longitude!! in 124.0..132.0)
            assertTrue(it.name, it.difficulty != null)
            assertTrue(it.name, it.provinces.isNotEmpty())
            assertTrue(it.name, RegionGroup.entries.any(it::isIn))
        }
    }

    @Test
    fun `courses are parsed with minutes`() {
        val withMinutes = mountains.count { it.recommendedCourse?.minutes != null }
        assertTrue("only $withMinutes have minutes", withMinutes >= 90)
    }

    @Test
    fun `difficulty is estimated only when no official grade exists`() {
        assertEquals(29, mountains.count { !it.difficultyEstimated })
    }

    @Test
    fun `estimate thresholds`() {
        assertEquals(Difficulty.EASY, estimateDifficulty(179))
        assertEquals(Difficulty.MODERATE, estimateDifficulty(180))
        assertEquals(Difficulty.MODERATE, estimateDifficulty(299))
        assertEquals(Difficulty.HARD, estimateDifficulty(300))
        assertEquals(Difficulty.MODERATE, estimateDifficulty(null))
    }
}
