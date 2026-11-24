package com.myhiking.log.presentation.mountain

import androidx.annotation.StringRes
import com.myhiking.log.R
import com.myhiking.log.domain.model.Difficulty
import com.myhiking.log.domain.model.HeightRange
import com.myhiking.log.domain.model.MountainView
import com.myhiking.log.domain.model.RegionGroup
import java.text.NumberFormat
import java.util.Locale

@get:StringRes
internal val MountainView.labelRes: Int
    get() = when (this) {
        MountainView.REGION -> R.string.mountains_view_region
        MountainView.DIFFICULTY -> R.string.mountains_view_difficulty
        MountainView.HEIGHT -> R.string.mountains_view_height
    }

@get:StringRes
internal val RegionGroup.labelRes: Int
    get() = when (this) {
        RegionGroup.CAPITAL -> R.string.region_capital
        RegionGroup.GANGWON -> R.string.region_gangwon
        RegionGroup.CHUNGCHEONG -> R.string.region_chungcheong
        RegionGroup.JEOLLA -> R.string.region_jeolla
        RegionGroup.GYEONGSANG -> R.string.region_gyeongsang
        RegionGroup.JEJU -> R.string.region_jeju
    }

@get:StringRes
internal val Difficulty.labelRes: Int
    get() = when (this) {
        Difficulty.EASY -> R.string.difficulty_easy
        Difficulty.MODERATE -> R.string.difficulty_moderate
        Difficulty.HARD -> R.string.difficulty_hard
    }

@get:StringRes
internal val HeightRange.labelRes: Int
    get() = when (this) {
        HeightRange.UNDER_500 -> R.string.height_under_500
        HeightRange.FROM_500_TO_1000 -> R.string.height_500_to_1000
        HeightRange.OVER_1000 -> R.string.height_over_1000
    }

// 835.6 -> "835m". truncate, not round (999.7 must stay in the 500~1,000m chip)
internal fun formatMeters(meters: Double): String =
    NumberFormat.getIntegerInstance(Locale.KOREA).format(meters.toLong()) + "m"
