package com.myhiking.log.data.remote.supabase

import com.myhiking.log.domain.model.CompanionType
import com.myhiking.log.domain.model.Weather
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class SummitRecordDtoTest {

    // same as supabase-kt default serializer
    private val json = Json { ignoreUnknownKeys = true }

    private val row = """
        {"id":"7f0c","user_id":"u1","mountain_id":"top100:북한산","mountain_name":"북한산",
         "mountain_height_meters":835.6,"is_top100":true,"provinces":["서울","경기"],
         "climbed_on":"2026-11-24","course":"우이동 → 백운대","duration_minutes":270,
         "weather":"partly_cloudy","companion":"friends","rating":4,"comment":"단풍 끝물",
         "created_at":"2026-11-24T09:12:45.123456+00:00","updated_at":"2026-11-24T09:12:45.123456+00:00",
         "deleted_at":null}
    """.trimIndent()

    @Test
    fun `row maps to domain`() {
        val record = json.decodeFromString<SummitRecordDto>(row).toDomain()
        assertEquals(LocalDate.of(2026, 11, 24), record.climbedOn)
        assertEquals(Weather.PARTLY_CLOUDY, record.weather)
        assertEquals(CompanionType.FRIENDS, record.companion)
        assertEquals(listOf("서울", "경기"), record.provinces)
        assertEquals(Instant.parse("2026-11-24T09:12:45.123456Z"), record.createdAt)
    }

    @Test
    fun `unknown enum value becomes null`() {
        val record = json.decodeFromString<SummitRecordDto>(row.replace("\"partly_cloudy\"", "\"foggy\"")).toDomain()
        assertNull(record.weather)
    }

    @Test
    fun `write row sends cleared fields as null`() {
        val record = json.decodeFromString<SummitRecordDto>(row).toDomain().copy(comment = null, weather = null)
        val body = json.encodeToJsonElement(record.toWriteDto("u1")).jsonObject
        assertEquals(JsonNull, body["comment"])
        assertEquals(JsonNull, body["weather"])
        assertEquals("friends", body["companion"]?.jsonPrimitive?.content)
        assertEquals("2026-11-24", body["climbed_on"]?.jsonPrimitive?.content)
        // server-managed columns not sent
        assertNull(body["created_at"])
        assertNull(body["deleted_at"])
    }

    @Test
    fun `db values match the SQL check`() {
        assertEquals(listOf("sunny", "partly_cloudy", "cloudy", "rainy", "snowy"), Weather.entries.map { it.dbValue })
        assertEquals(listOf("alone", "friends", "family", "partner", "group"), CompanionType.entries.map { it.dbValue })
    }
}
