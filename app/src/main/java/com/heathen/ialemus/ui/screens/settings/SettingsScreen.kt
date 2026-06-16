package com.heathen.ialemus.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.settings.SettingsPlaceholder
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudSectionLabel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
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
        HudHeader(
            title = "Settings",
            statusLabel = "DAP MODE",
            subtitle = "Ialemus MVP 1A EVA HUD Pass",
        )

        HudPanel(
            title = "Local Library",
            sectionTag = "LOCAL SIGNAL",
            subtitle = "$trackCount tracks indexed on device.",
        ) {
            HudStatusChip(label = "$trackCount tracks", highlighted = trackCount > 0)
        }

        HudPanel(
            title = "DAP Low-Power Mode",
            sectionTag = "DAP MODE",
            subtitle = "Reduces grid/scanline overlays and motion for battery-friendly visuals on HiBy R4 and phones.",
        ) {
            RowSwitch(
                label = "Enable reduced-motion HUD",
                checked = dapMode,
                onCheckedChange = settingsViewModel::setDapMode,
            )
        }

        HudSectionLabel(label = "Theme Select", trailing = selectedTheme.displayName.uppercase())

        HudPanel(title = "EVA Themes", sectionTag = "COMMAND DOCK") {
            ThemeChipRow(
                themes = ThemeId.evaThemes,
                selectedTheme = selectedTheme,
                onSelect = settingsViewModel::setTheme,
            )
        }

        HudPanel(title = "Ialemus Original", sectionTag = "LEGACY PALETTE") {
            ThemeChipRow(
                themes = ThemeId.ialemusThemes,
                selectedTheme = selectedTheme,
                onSelect = settingsViewModel::setTheme,
            )
        }

        ExposedDropdownMenuBox(
            expanded = themeMenuExpanded,
            onExpandedChange = { themeMenuExpanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = selectedTheme.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Theme (dropdown)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            DropdownMenu(
                expanded = themeMenuExpanded,
                onDismissRequest = { themeMenuExpanded = false },
            ) {
                ThemeId.evaThemes.forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.displayName) },
                        onClick = {
                            settingsViewModel.setTheme(theme)
                            themeMenuExpanded = false
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text("— Ialemus Original —") },
                    onClick = {},
                    enabled = false,
                )
                ThemeId.ialemusThemes.forEach { theme ->
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

        HudPanel(
            title = "NAS Bridge",
            sectionTag = "FUTURE MODULE",
            subtitle = "Future integration (MVP 2). No shell/SSH/Docker from Android.",
        ) {
            HudStatusChip(label = "NAS Bridge required", disabled = true)
        }

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

        HudButton(
            label = "Test connection (MVP 2)",
            onClick = { },
            enabled = false,
        )

        HudPanel(
            title = "About",
            sectionTag = "PLAYBACK CORE",
            subtitle = "Ialemus MVP 1A EVA HUD Pass\nVersion ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nOriginal EVA-inspired styling — no official/copyrighted assets.",
        ) {
            Text(
                text = "Default theme: EVA-01 Berserk",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
            )
        }
    }
}

@Composable
private fun ThemeChipRow(
    themes: List<ThemeId>,
    selectedTheme: ThemeId,
    onSelect: (ThemeId) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themes.forEach { theme ->
            val selected = theme == selectedTheme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(theme) }
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) {
                            LocalIalemusTokens.current.accentActive
                        } else {
                            LocalIalemusTokens.current.hudBorderColor.copy(alpha = 0.35f)
                        },
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) {
                        LocalIalemusTokens.current.glowColor
                    } else {
                        LocalIalemusTokens.current.textMuted
                    },
                )
                HudStatusChip(
                    label = if (selected) "ACTIVE" else "SELECT",
                    highlighted = selected,
                )
            }
        }
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
