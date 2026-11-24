package com.myhiking.log.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// acorn forest palette. moss = fills, acorn brown = text accents
object AcornColors {
    val Cream = Color(0xFFFCFAF3)
    val Surface = Color(0xFFFFFFFF)
    val MossLight = Color(0xFFEEF2E1)
    val Moss = Color(0xFFC5D69A)
    // charts (past months), empty stamps
    val MossSoft = Color(0xFFDCE6C0)
    val Forest = Color(0xFF4F6B3A)
    val Acorn = Color(0xFFC98A4B)
    val AcornBrown = Color(0xFF7A4A21)
    val Ink = Color(0xFF2F2A1F)
    val InkMuted = Color(0xFF6B6452)
    val Outline = Color(0xFFE8E4D3)
    val OutlineStrong = Color(0xFFBDB79C)
    val Saturday = Color(0xFF1976D2)
    // card / FAB shadow
    val Shadow = AcornBrown.copy(alpha = 0.25f)
}
