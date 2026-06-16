package com.heathen.ialemus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import com.heathen.ialemus.core.model.ThemeId

private val IalemusTypography = Typography()

@Composable
fun IalemusTheme(
    themeId: ThemeId = ThemeId.DEFAULT,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(themeId),
        typography = IalemusTypography,
        content = content,
    )
}
