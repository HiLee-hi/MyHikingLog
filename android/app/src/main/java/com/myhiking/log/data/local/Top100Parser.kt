package com.myhiking.log.data.local

import com.myhiking.log.data.splitTagline
import com.myhiking.log.data.tidyAddress
import com.myhiking.log.data.tidyCourseLabel
import com.myhiking.log.data.tidyDuration
import com.myhiking.log.data.tidyRoute
import com.myhiking.log.data.tidyText
import com.myhiking.log.domain.model.Difficulty
import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.model.MountainCourse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// assets/top100_mountains.json (tools/convert_top100.py)
@Serializable
internal data class Top100MountainDto(
    val id: String,
    val name: String,
    val address: String,
    val regions: List<String>,
    val heightMeters: Double,
    val latitude: Double,
    val longitude: Double,
    val hikingTime: String? = null,
    val officialDifficulty: String? = null,
    val selectionReason: String,
    val overview: String,
    val hikingPoints: String,
    val courses: List<Top100CourseDto>,
    val transport: String
)

@Serializable
internal data class Top100CourseDto(
    val label: String,
    val route: String,
    val duration: String,
    val minutes: Int? = null
)

private val json = Json { ignoreUnknownKeys = true }

// no Android types here -> JVM test reads the real asset
fun parseTop100(text: String): List<Mountain> =
    json.decodeFromString<List<Top100MountainDto>>(text).map { it.toDomain() }

private fun Top100MountainDto.toDomain(): Mountain {
    val courses = courses.map { MountainCourse(tidyCourseLabel(it.label), tidyRoute(it.route), tidyDuration(it.duration), it.minutes) }
    val (tagline, overviewBody) = splitTagline(tidyText(overview))
    val official = when (officialDifficulty) {
        "초급" -> Difficulty.EASY
        "중급" -> Difficulty.MODERATE
        "고급", "상급" -> Difficulty.HARD
        else -> null
    }
    val recommended = courses.firstOrNull { "추천" in it.label } ?: courses.firstOrNull()
    return Mountain(
        id = id,
        name = name,
        address = tidyAddress(address),
        provinces = regions,
        heightMeters = heightMeters,
        latitude = latitude,
        longitude = longitude,
        isTop100 = true,
        hikingTime = hikingTime,
        difficulty = official ?: estimateDifficulty(recommended?.minutes),
        difficultyEstimated = official == null,
        selectionReason = tidyText(selectionReason),
        tagline = tagline,
        overview = overviewBody,
        hikingPoints = tidyText(hikingPoints),
        courses = courses,
        transport = tidyText(transport)
    )
}

// recommended course time: <3h easy, <5h moderate, else hard
internal fun estimateDifficulty(minutes: Int?): Difficulty = when {
    minutes == null -> Difficulty.MODERATE
    minutes < 180 -> Difficulty.EASY
    minutes < 300 -> Difficulty.MODERATE
    else -> Difficulty.HARD
}
