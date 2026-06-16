package com.heathen.ialemus.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val tokens = LocalIalemusTokens.current
    val colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = tokens.textPrimary,
        unfocusedTextColor = tokens.textPrimary,
        disabledTextColor = tokens.textMuted,
        errorTextColor = tokens.textPrimary,
        focusedLabelColor = tokens.accentActive,
        unfocusedLabelColor = tokens.textMuted,
        disabledLabelColor = tokens.textMuted,
        errorLabelColor = tokens.warningColor,
        focusedPlaceholderColor = tokens.textMuted.copy(alpha = 0.65f),
        unfocusedPlaceholderColor = tokens.textMuted.copy(alpha = 0.55f),
        disabledPlaceholderColor = tokens.textMuted.copy(alpha = 0.4f),
        focusedBorderColor = tokens.accentActive,
        unfocusedBorderColor = tokens.hudBorderColor.copy(alpha = 0.55f),
        disabledBorderColor = tokens.hudBorderColor.copy(alpha = 0.25f),
        errorBorderColor = tokens.warningColor,
        focusedSupportingTextColor = tokens.textMuted,
        unfocusedSupportingTextColor = tokens.textMuted,
        errorSupportingTextColor = tokens.warningColor,
        cursorColor = tokens.accentActive,
        errorCursorColor = tokens.warningColor,
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        colors = colors,
    )
}
