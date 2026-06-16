package com.heathen.ialemus.core.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.ThemeId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val settingsRepository = container.settingsRepository

    val themeId: StateFlow<ThemeId> = settingsRepository.themeId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeId.DEFAULT,
    )

    val dapModeEnabled: StateFlow<Boolean> = settingsRepository.dapModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val trackCount: StateFlow<Int> = container.libraryRepository.trackCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    fun setTheme(themeId: ThemeId) {
        viewModelScope.launch { settingsRepository.setTheme(themeId) }
    }

    fun setDapMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDapMode(enabled) }
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(container) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
