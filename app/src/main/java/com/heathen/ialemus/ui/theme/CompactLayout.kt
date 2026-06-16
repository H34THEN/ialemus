package com.heathen.ialemus.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CompactLayout {
    const val COMPACT_WIDTH_DP = 420
    const val DOCK_LABEL_WIDTH_DP = 480

    val screenHorizontalPadding: Dp = 8.dp
    val screenHorizontalPaddingWide: Dp = 12.dp
    val miniPlayerArtSize: Dp = 40.dp
    val miniTransportIconSize: Dp = 22.dp
    val miniTransportButtonSize: Dp = 36.dp
    val dockIconSize: Dp = 26.dp
    val dockLabelSize: Dp = 10.dp
    val dockLabelFontSize = 10.sp
    val dockMinTouchTarget: Dp = 48.dp
    val dockVerticalPadding: Dp = 6.dp
    val dockIndicatorWidth: Dp = 20.dp
    val dockIndicatorHeight: Dp = 3.dp
    val bottomChromePadding: Dp = 8.dp
}

@Composable
fun isCompactWidth(): Boolean {
    return LocalConfiguration.current.screenWidthDp < CompactLayout.COMPACT_WIDTH_DP
}

@Composable
fun showDockLabels(): Boolean {
    return LocalConfiguration.current.screenWidthDp >= CompactLayout.DOCK_LABEL_WIDTH_DP
}

@Composable
fun screenHorizontalPadding(): Dp {
    return if (isCompactWidth()) CompactLayout.screenHorizontalPadding else CompactLayout.screenHorizontalPaddingWide
}
