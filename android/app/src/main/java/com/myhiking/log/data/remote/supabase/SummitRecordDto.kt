package com.myhiking.log.data.remote.supabase

import com.myhiking.log.domain.model.CompanionType
import com.myhiking.log.domain.model.SummitRecord
import com.myhiking.log.domain.model.Weather
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

// read row. timestamps set by the server
@Serializable
data class SummitRecordDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("mountain_id") val mountainId: String,
    @SerialName("mountain_name") val mountainName: String,
    @SerialName("mountain_height_meters") val mountainHeightMeters: Double? = null,
    @SerialName("is_top100") val isTop100: Boolean = false,
    @SerialName("provinces") val provinces: List<String> = emptyList(),
    @SerialName("climbed_on") val climbedOn: String,
    @SerialName("course") val course: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("weather") val weather: String? = null,
    @SerialName("companion") val companion: String? = null,
    @SerialName("rating") val rating: Int? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// write row. no defaults -> every column is sent, so clearing a field sends null
// (supabase-kt Json skips default values)
@Serializable
data class SummitRecordWriteDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("mountain_id") val mountainId: String,
    @SerialName("mountain_name") val mountainName: String,
    @SerialName("mountain_height_meters") val mountainHeightMeters: Double?,
    @SerialName("is_top100") val isTop100: Boolean,
    @SerialName("provinces") val provinces: List<String>,
    @SerialName("climbed_on") val climbedOn: String,
    @SerialName("course") val course: String?,
    @SerialName("duration_minutes") val durationMinutes: Int?,
    @SerialName("weather") val weather: String?,
    @SerialName("companion") val companion: String?,
    @SerialName("rating") val rating: Int?,
    @SerialName("comment") val comment: String?
)

// unknown enum value (newer app version) -> null instead of failing the whole list
fun SummitRecordDto.toDomain() = SummitRecord(
    id = id,
    mountainId = mountainId,
    mountainName = mountainName,
    mountainHeightMeters = mountainHeightMeters,
    isTop100 = isTop100,
    provinces = provinces,
    climbedOn = LocalDate.parse(climbedOn),
    course = course,
    durationMinutes = durationMinutes,
    weather = weather?.let { value -> Weather.entries.firstOrNull { it.dbValue == value } },
    companion = companion?.let { value -> CompanionType.entries.firstOrNull { it.dbValue == value } },
    rating = rating,
    comment = comment,
    createdAt = createdAt?.let(::parseTimestamp),
    updatedAt = updatedAt?.let(::parseTimestamp)
)

fun SummitRecord.toWriteDto(userId: String) = SummitRecordWriteDto(
    id = id,
    userId = userId,
    mountainId = mountainId,
    mountainName = mountainName,
    mountainHeightMeters = mountainHeightMeters,
    isTop100 = isTop100,
    provinces = provinces,
    climbedOn = climbedOn.toString(),
    course = course,
    durationMinutes = durationMinutes,
    weather = weather?.dbValue,
    companion = companion?.dbValue,
    rating = rating,
    comment = comment
)

// "sunny", "partly_cloudy" ... same as the SQL check
internal val Weather.dbValue: String get() = name.lowercase()
internal val CompanionType.dbValue: String get() = name.lowercase()

// PostgREST: "2026-11-25T03:12:45.123456+00:00"
internal fun parseTimestamp(text: String): Instant? =
    runCatching { OffsetDateTime.parse(text).toInstant() }.getOrNull()
