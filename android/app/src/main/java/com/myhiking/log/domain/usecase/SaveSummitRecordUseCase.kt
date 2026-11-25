package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.domain.repository.SummitRecordRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

enum class SummitRecordError { FUTURE_DATE, INVALID_DURATION, INVALID_RATING, COMMENT_TOO_LONG }

class InvalidSummitRecordException(val errors: Set<SummitRecordError>) :
    IllegalArgumentException("invalid summit record: $errors")

// blank id -> new record. blank text -> null
class SaveSummitRecordUseCase @Inject constructor(
    private val repository: SummitRecordRepository
) {
    suspend operator fun invoke(record: SummitRecord, today: LocalDate = LocalDate.now()): SummitRecord {
        val tidy = tidy(record)
        val errors = validate(tidy, today)
        if (errors.isNotEmpty()) throw InvalidSummitRecordException(errors)
        return repository.save(tidy)
    }

    companion object {
        // max 48h, overnight trips
        const val MAX_DURATION_MINUTES = 48 * 60

        private fun tidy(record: SummitRecord): SummitRecord = record.copy(
            id = record.id.ifBlank { UUID.randomUUID().toString() },
            course = record.course?.trim()?.ifEmpty { null },
            comment = record.comment?.trim()?.ifEmpty { null }
        )

        fun validate(record: SummitRecord, today: LocalDate): Set<SummitRecordError> = buildSet {
            if (record.climbedOn.isAfter(today)) add(SummitRecordError.FUTURE_DATE)
            record.durationMinutes?.let { if (it !in 1..MAX_DURATION_MINUTES) add(SummitRecordError.INVALID_DURATION) }
            record.rating?.let { if (it !in 1..SummitRecord.MAX_RATING) add(SummitRecordError.INVALID_RATING) }
            record.comment?.let { if (it.trim().length > SummitRecord.MAX_COMMENT_LENGTH) add(SummitRecordError.COMMENT_TOO_LONG) }
        }
    }
}
