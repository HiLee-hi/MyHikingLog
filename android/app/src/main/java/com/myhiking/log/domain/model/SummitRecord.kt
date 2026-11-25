package com.myhiking.log.domain.model

import java.time.Instant
import java.time.LocalDate

enum class Weather { SUNNY, PARTLY_CLOUDY, CLOUDY, RAINY, SNOWY }

enum class CompanionType { ALONE, FRIENDS, FAMILY, PARTNER, GROUP }

// mountain fields are a snapshot (API mountains can't be looked up by id later)
data class SummitRecord(
    val id: String,
    val mountainId: String,
    val mountainName: String,
    val mountainHeightMeters: Double?,
    val isTop100: Boolean,
    val provinces: List<String>,
    val climbedOn: LocalDate,
    val course: String?,
    val durationMinutes: Int?,
    val weather: Weather?,
    val companion: CompanionType?,
    // 1..5
    val rating: Int?,
    val comment: String?,
    // null until saved
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {
    companion object {
        const val MAX_RATING = 5
        const val MAX_COMMENT_LENGTH = 100
    }
}

enum class LoadStatus { IDLE, LOADING, LOADED, FAILED }

// newest climb first
data class SummitRecords(
    val status: LoadStatus = LoadStatus.IDLE,
    val items: List<SummitRecord> = emptyList()
) {
    fun forMountain(mountainId: String): List<SummitRecord> = items.filter { it.mountainId == mountainId }
}

// newest climb first, same day -> newest saved first
internal val SummitRecordOrder: Comparator<SummitRecord> =
    compareByDescending<SummitRecord> { it.climbedOn }.thenByDescending { it.createdAt ?: Instant.MAX }
