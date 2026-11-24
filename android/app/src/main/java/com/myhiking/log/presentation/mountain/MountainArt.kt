package com.myhiking.log.presentation.mountain

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.clearAndSetSemantics

// sky, far ridge, near ridge. varies per card
private val Tones = listOf(
    Triple(Color(0xFFDCE6C0), Color(0xFF8FA56A), Color(0xFF4F6B3A)),
    Triple(Color(0xFFE4EAD8), Color(0xFFA9B98A), Color(0xFF6E8F4E)),
    Triple(Color(0xFFF3E3C8), Color(0xFFC98A4B), Color(0xFF7A4A21)),
)

// placeholder art when there's no photo
@Composable
fun MountainArt(seed: Int, modifier: Modifier = Modifier) {
    val (sky, far, near) = Tones[Math.floorMod(seed, Tones.size)]
    Box(modifier = modifier.background(sky).clearAndSetSemantics { }) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            fun ridge(points: List<Pair<Float, Float>>) = Path().apply {
                moveTo(0f, h)
                points.forEach { (x, y) -> lineTo(x * w, y * h) }
                lineTo(w, h)
                close()
            }
            drawPath(ridge(listOf(0f to 0.70f, 0.24f to 0.38f, 0.40f to 0.54f, 0.62f to 0.24f, 1f to 0.68f)), far)
            drawPath(ridge(listOf(0f to 0.86f, 0.28f to 0.58f, 0.48f to 0.74f, 0.66f to 0.62f, 1f to 0.86f)), near)
        }
    }
}
