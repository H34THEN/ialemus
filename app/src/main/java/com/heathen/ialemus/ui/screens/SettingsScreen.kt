package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.settings.SettingsPlaceholder
import com.heathen.ialemus.ui.components.PlaceholderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedTheme: ThemeId,
    onThemeSelected: (ThemeId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var bridgeUrl by rememberSaveable { mutableStateOf("http://nas.local:8787/api") }
    var musicRoot by rememberSaveable { mutableStateOf("/volume1/Music") }
    var themeMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedTextField(
            value = bridgeUrl,
            onValueChange = { bridgeUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ialemus Bridge URL") },
            placeholder = { Text("http://nas.local:8787/api") },
            singleLine = true,
            readOnly = true,
        )

        OutlinedTextField(
            value = SettingsPlaceholder.TOKEN_MASK,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API token") },
            placeholder = { Text("replace_me") },
            singleLine = true,
            readOnly = true,
        )

        OutlinedTextField(
            value = musicRoot,
            onValueChange = { musicRoot = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Music library root") },
            placeholder = { Text("/volume1/Music") },
            singleLine = true,
            readOnly = true,
        )

        Button(
            onClick = { /* TODO(MVP 2): GET /health */ },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Test connection (placeholder)")
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
                label = { Text("Theme") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = themeMenuExpanded,
                onDismissRequest = { themeMenuExpanded = false },
            ) {
                ThemeId.entries.forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.displayName) },
                        onClick = {
                            onThemeSelected(theme)
                            themeMenuExpanded = false
                        },
                    )
                }
            }
        }

        PlaceholderCard(
            title = "About",
            body = "Ialemus MVP 0\nVersion ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        )

        // TODO: Encrypted token storage (MVP 2). Landscape two-column settings (MVP 5).
    }
}
