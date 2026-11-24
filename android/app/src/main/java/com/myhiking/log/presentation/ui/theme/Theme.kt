package com.myhiking.log.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// primary = acorn brown (moss is too light for text buttons/tabs)
// moss: filled buttons (acornButtonColors), nav indicator (secondaryContainer)
private val AcornColorScheme = lightColorScheme(
    primary = AcornColors.AcornBrown,
    onPrimary = Color.White,
    primaryContainer = AcornColors.Moss,
    onPrimaryContainer = AcornColors.Ink,
    secondary = AcornColors.Forest,
    onSecondary = Color.White,
    secondaryContainer = AcornColors.Moss,
    onSecondaryContainer = AcornColors.Ink,
    tertiary = AcornColors.InkMuted,
    onTertiary = Color.White,
    tertiaryContainer = AcornColors.MossLight,
    onTertiaryContainer = AcornColors.Ink,
    background = AcornColors.Cream,
    onBackground = AcornColors.Ink,
    surface = AcornColors.Cream,
    onSurface = AcornColors.Ink,
    surfaceVariant = AcornColors.MossLight,
    onSurfaceVariant = AcornColors.InkMuted,
    surfaceContainerLowest = AcornColors.Surface,
    surfaceContainerLow = AcornColors.Surface,
    surfaceContainer = AcornColors.Surface,
    surfaceContainerHigh = AcornColors.MossLight,
    surfaceContainerHighest = AcornColors.MossLight,
    inverseSurface = AcornColors.Ink,
    inverseOnSurface = AcornColors.Cream,
    outline = AcornColors.OutlineStrong,
    outlineVariant = AcornColors.Outline,
)

private val AcornShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// light only, no dynamic color
@Composable
fun MyHikingLogTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = AcornColorScheme,
        typography = AcornTypography,
        shapes = AcornShapes,
        content = content
    )
}

// main button
@Composable
fun acornButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
)
