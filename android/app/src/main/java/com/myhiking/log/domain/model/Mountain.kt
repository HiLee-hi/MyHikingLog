package com.myhiking.log.domain.model

// course as in the source (label, route, duration text)
data class MountainCourse(
    val label: String,
    val route: String,
    val duration: String,
    val minutes: Int?
)

enum class Difficulty { EASY, MODERATE, HARD }

// region filter groups. a mountain can be in more than one
enum class RegionGroup(val provinces: Set<String>) {
    CAPITAL(setOf("서울", "경기", "인천")),
    GANGWON(setOf("강원")),
    CHUNGCHEONG(setOf("대전", "세종", "충북", "충남")),
    JEOLLA(setOf("광주", "전북", "전남")),
    GYEONGSANG(setOf("부산", "대구", "울산", "경북", "경남")),
    JEJU(setOf("제주"));
}

enum class HeightRange(val minMeters: Double, val maxMeters: Double) {
    UNDER_500(0.0, 500.0),
    FROM_500_TO_1000(500.0, 1000.0),
    OVER_1000(1000.0, Double.MAX_VALUE);

    operator fun contains(meters: Double): Boolean = meters >= minMeters && meters < maxMeters
}

// top 100: all fields. API mountains: no coords/difficulty/courses/transport, height sometimes 0
data class Mountain(
    val id: String,
    val name: String,
    val address: String,
    // 서울, 경기, 강원 ... from address
    val provinces: List<String>,
    val heightMeters: Double?,
    val latitude: Double?,
    val longitude: Double?,
    val isTop100: Boolean,
    val hikingTime: String?,
    val difficulty: Difficulty?,
    // difficulty from course time, not official
    val difficultyEstimated: Boolean,
    // top 100: 선정 이유 / API: mntitop
    val selectionReason: String,
    // "[...]" title at the start of the CSV overview. null for API
    val tagline: String? = null,
    val overview: String,
    val hikingPoints: String,
    val courses: List<MountainCourse>,
    val transport: String,
    // mntilistno. null for top 100
    val forestListNo: String? = null,
    // managing office + phone (API)
    val manager: String? = null
) {
    // "추천" course, else first
    val recommendedCourse: MountainCourse?
        get() = courses.firstOrNull { "추천" in it.label } ?: courses.firstOrNull()

    fun isIn(group: RegionGroup): Boolean = provinces.any { it in group.provinces }

    // "북한산 백운대" -> "북한산"
    val baseName: String
        get() = name.substringBefore(' ')
}

private val ProvinceNames = listOf(
    "서울특별시" to "서울", "부산광역시" to "부산", "대구광역시" to "대구", "인천광역시" to "인천",
    "광주광역시" to "광주", "대전광역시" to "대전", "울산광역시" to "울산", "세종특별자치시" to "세종",
    "경기도" to "경기", "강원특별자치도" to "강원", "강원도" to "강원", "충청북도" to "충북",
    "충청남도" to "충남", "전북특별자치도" to "전북", "전라북도" to "전북", "전라남도" to "전남",
    "경상북도" to "경북", "경상남도" to "경남", "제주특별자치도" to "제주",
)

// same table as tools/convert_top100.py
fun provincesIn(address: String): List<String> =
    ProvinceNames.filter { (full, _) -> full in address }.map { it.second }.distinct()
