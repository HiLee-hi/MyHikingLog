package com.myhiking.log.presentation.summit

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.myhiking.log.R
import com.myhiking.log.domain.model.CompanionType
import com.myhiking.log.domain.model.Weather

@get:StringRes
internal val Weather.labelRes: Int
    get() = when (this) {
        Weather.SUNNY -> R.string.weather_sunny
        Weather.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
        Weather.CLOUDY -> R.string.weather_cloudy
        Weather.RAINY -> R.string.weather_rainy
        Weather.SNOWY -> R.string.weather_snowy
    }

@get:StringRes
internal val CompanionType.labelRes: Int
    get() = when (this) {
        CompanionType.ALONE -> R.string.companion_alone
        CompanionType.FRIENDS -> R.string.companion_friends
        CompanionType.FAMILY -> R.string.companion_family
        CompanionType.PARTNER -> R.string.companion_partner
        CompanionType.GROUP -> R.string.companion_group
    }

// 270 -> "4시간 30분"
@Composable
internal fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> stringResource(R.string.duration_minutes, m)
        m == 0 -> stringResource(R.string.duration_hours, h)
        else -> stringResource(R.string.duration_hours_minutes, h, m)
    }
}

// both blank -> null (not entered). non-number -> -1 so validation flags it
internal fun parseDuration(hours: String, minutes: String): Int? {
    val h = hours.trim()
    val m = minutes.trim()
    if (h.isEmpty() && m.isEmpty()) return null
    val hv = if (h.isEmpty()) 0 else h.toIntOrNull() ?: return -1
    val mv = if (m.isEmpty()) 0 else m.toIntOrNull() ?: return -1
    if (hv < 0 || mv !in 0..59) return -1
    return hv * 60 + mv
}

// minutes -> (hours, minutes) field text
internal fun durationFields(minutes: Int?): Pair<String, String> =
    if (minutes == null) "" to "" else (minutes / 60).toString() to (minutes % 60).toString()
