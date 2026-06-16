package com.heathen.ialemus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.heathen.ialemus.core.model.ThemeId

@Composable
fun IalemusTheme(
    themeId: ThemeId = ThemeId.DEFAULT,
    dapMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val tokens = tokensFor(themeId, dapMode)
    CompositionLocalProvider(LocalIalemusTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorSchemeFor(themeId),
            typography = HudTypography,
            shapes = shapesFor(themeId),
            content = content,
        )
    }
}
