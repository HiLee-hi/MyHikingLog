package com.myhiking.log.presentation.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// content sizes for adaptive layouts. window decisions are in AdaptiveLayoutInfo
object AdaptiveDimens {
    // M3 pane spacer
    val PaneSpacing: Dp = 24.dp

    // M3 rail width
    val NavigationRailWidth: Dp = 80.dp

    // list pane
    val ListPaneWidth: Dp = 360.dp

    // below this -> single pane
    val DetailPaneMinWidth: Dp = 360.dp

    // 3rd pane on Large
    val ExtraPaneMinWidth: Dp = 360.dp

    // min column width
    val ColumnMinWidth: Dp = 300.dp

    // max text width
    val ReadableMaxWidth: Dp = 640.dp

    // max content width (tablet, 4:3)
    val ContentMaxWidth: Dp = 1040.dp

    // grid cell
    val PosterGridMinCellWidth: Dp = 108.dp
    val PhotoGridMinCellWidth: Dp = 112.dp

    // 2 columns + spacer fit?
    fun fitsTwoColumns(availableWidth: Dp, minColumnWidth: Dp = ColumnMinWidth): Boolean =
        availableWidth >= minColumnWidth * 2 + PaneSpacing
}
