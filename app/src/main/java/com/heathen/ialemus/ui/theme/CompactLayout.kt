package com.heathen.ialemus.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CompactLayout {
    const val COMPACT_WIDTH_DP = 420

    val screenHorizontalPadding: Dp = 8.dp
    val screenHorizontalPaddingWide: Dp = 12.dp
    val miniPlayerArtSize: Dp = 40.dp
    val miniTransportIconSize: Dp = 22.dp
    val miniTransportButtonSize: Dp = 36.dp
    val dockIconSize: Dp = 24.dp
    val dockItemPadding: Dp = 4.dp
    val bottomChromePadding: Dp = 8.dp
}

@Composable
fun isCompactWidth(): Boolean {
    return LocalConfiguration.current.screenWidthDp < CompactLayout.COMPACT_WIDTH_DP
}

@Composable
fun screenHorizontalPadding(): Dp {
    return if (isCompactWidth()) CompactLayout.screenHorizontalPadding else CompactLayout.screenHorizontalPaddingWide
}
