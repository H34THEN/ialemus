package com.heathen.ialemus.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.settings.SettingsPlaceholder
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.components.PlaceholderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val selectedTheme by settingsViewModel.themeId.collectAsStateWithLifecycle()
    val dapMode by settingsViewModel.dapModeEnabled.collectAsStateWithLifecycle()
    val trackCount by settingsViewModel.trackCount.collectAsStateWithLifecycle()
    var themeMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)

        PlaceholderCard(
            title = "Local library",
            body = "$trackCount tracks indexed on device.",
        )

        RowSwitch(
            label = "DAP low-power mode",
            checked = dapMode,
            onCheckedChange = settingsViewModel::setDapMode,
        )

        ExposedDropdownMenuBox(
            expanded = themeMenuExpanded,
            onExpandedChange = { themeMenuExpanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = selectedTheme.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Theme") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            DropdownMenu(
                expanded = themeMenuExpanded,
                onDismissRequest = { themeMenuExpanded = false },
            ) {
                ThemeId.entries.forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.displayName) },
                        onClick = {
                            settingsViewModel.setTheme(theme)
                            themeMenuExpanded = false
                        },
                    )
                }
            }
        }

        PlaceholderCard(
            title = "NAS Bridge (future integration)",
            body = "Bridge URL, token, and connection test arrive in MVP 2. No shell/SSH/Docker from Android.",
        )

        OutlinedTextField(
            value = "http://nas.local:8787/api",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ialemus Bridge URL") },
            readOnly = true,
            enabled = false,
        )

        OutlinedTextField(
            value = SettingsPlaceholder.TOKEN_MASK,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API token") },
            readOnly = true,
            enabled = false,
        )

        Button(
            onClick = { /* MVP 2 */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
        ) {
            Text("Test connection (MVP 2)")
        }

        PlaceholderCard(
            title = "About",
            body = "Ialemus MVP 1A\nVersion ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        )
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
