package com.myhiking.log.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import com.myhiking.log.R

// Jua for titles, system font for body
val Jua = FontFamily(Font(R.font.jua_regular, FontWeight.Normal))

// Jua has one weight, no fake bold
private fun TextStyle.jua() = copy(
    fontFamily = Jua,
    fontWeight = FontWeight.Normal,
    fontSynthesis = FontSynthesis.None
)

internal val AcornTypography = Typography().run {
    copy(
        displayLarge = displayLarge.jua(),
        displayMedium = displayMedium.jua(),
        displaySmall = displaySmall.jua(),
        headlineLarge = headlineLarge.jua(),
        headlineMedium = headlineMedium.jua(),
        headlineSmall = headlineSmall.jua(),
        titleLarge = titleLarge.jua(),
    )
}
