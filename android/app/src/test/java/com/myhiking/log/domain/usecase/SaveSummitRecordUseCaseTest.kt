package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.domain.model.SummitRecords
import com.myhiking.log.domain.repository.SummitRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate

class SaveSummitRecordUseCaseTest {

    private class FakeRepository : SummitRecordRepository {
        val saved = mutableListOf<SummitRecord>()
        override val records: StateFlow<SummitRecords> = MutableStateFlow(SummitRecords())
        override suspend fun refresh() = Unit
        override suspend fun save(record: SummitRecord) = record.also { saved += it }
        override suspend fun delete(id: String) = Unit
        override fun clear() = Unit
    }

    private val today = LocalDate.of(2026, 11, 25)

    private fun record(
        id: String = "",
        climbedOn: LocalDate = today,
        durationMinutes: Int? = null,
        rating: Int? = null,
        course: String? = null,
        comment: String? = null
    ) = SummitRecord(
        id = id, mountainId = "top100:북한산", mountainName = "북한산", mountainHeightMeters = 835.6,
        isTop100 = true, provinces = listOf("서울", "경기"), climbedOn = climbedOn, course = course,
        durationMinutes = durationMinutes, weather = null, companion = null, rating = rating, comment = comment
    )

    @Test
    fun `new record gets an id and blank text becomes null`() = runTest {
        val repository = FakeRepository()
        val saved = SaveSummitRecordUseCase(repository)(record(course = "  ", comment = " 좋았어요 "), today)
        assertTrue(saved.id.isNotBlank())
        assertNull(saved.course)
        assertEquals("좋았어요", saved.comment)
        assertEquals(listOf(saved), repository.saved)
    }

    @Test
    fun `existing id is kept`() = runTest {
        val saved = SaveSummitRecordUseCase(FakeRepository())(record(id = "abc"), today)
        assertEquals("abc", saved.id)
    }

    @Test
    fun `valid record has no errors`() {
        val ok = record(durationMinutes = 270, rating = 5, comment = "가".repeat(SummitRecord.MAX_COMMENT_LENGTH))
        assertEquals(emptySet<SummitRecordError>(), SaveSummitRecordUseCase.validate(ok, today))
    }

    @Test
    fun `bad fields are reported`() {
        val bad = record(
            climbedOn = today.plusDays(1),
            durationMinutes = 0,
            rating = 6,
            comment = "가".repeat(SummitRecord.MAX_COMMENT_LENGTH + 1)
        )
        assertEquals(SummitRecordError.entries.toSet(), SaveSummitRecordUseCase.validate(bad, today))
        assertEquals(
            setOf(SummitRecordError.INVALID_DURATION),
            SaveSummitRecordUseCase.validate(record(durationMinutes = SaveSummitRecordUseCase.MAX_DURATION_MINUTES + 1), today)
        )
    }

    @Test
    fun `invalid record is not saved`() = runTest {
        val repository = FakeRepository()
        try {
            SaveSummitRecordUseCase(repository)(record(rating = 0), today)
            fail("expected InvalidSummitRecordException")
        } catch (e: InvalidSummitRecordException) {
            assertEquals(setOf(SummitRecordError.INVALID_RATING), e.errors)
        }
        assertTrue(repository.saved.isEmpty())
    }
}
