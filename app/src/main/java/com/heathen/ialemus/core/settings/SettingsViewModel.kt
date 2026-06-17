package com.heathen.ialemus.core.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.model.NowPlayingVisualizerMode
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.network.ServiceUrlTester
import com.heathen.ialemus.core.network.ServiceUrlValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    val showMiniPlayerBar: StateFlow<Boolean> = settingsRepository.showMiniPlayerBar.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val nowPlayingLayoutMode: StateFlow<NowPlayingLayoutMode> =
        settingsRepository.nowPlayingLayoutMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NowPlayingLayoutMode.BALANCED,
        )

    val nowPlayingVisualizerMode: StateFlow<NowPlayingVisualizerMode> =
        settingsRepository.nowPlayingVisualizerMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NowPlayingVisualizerMode.SIGNAL_BARS,
        )

    val reactiveVisualizerEnabled: StateFlow<Boolean> =
        settingsRepository.reactiveVisualizerEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val trackCount: StateFlow<Int> = container.libraryRepository.trackCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    val nasConnectionSettings: StateFlow<NasConnectionSettings> =
        settingsRepository.nasConnectionSettings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NasConnectionSettings(),
        )

    private val _urlValidationError = MutableStateFlow<String?>(null)
    val urlValidationError: StateFlow<String?> = _urlValidationError.asStateFlow()

    private val _bridgeTestResult = MutableStateFlow<Result<Int>?>(null)
    private val _meTubeTestResult = MutableStateFlow<Result<Int>?>(null)
    private val _slskdTestResult = MutableStateFlow<Result<Int>?>(null)
    private val _nasUiTestResult = MutableStateFlow<Result<Int>?>(null)
    private val _bridgeChecking = MutableStateFlow(false)
    private val _meTubeChecking = MutableStateFlow(false)
    private val _slskdChecking = MutableStateFlow(false)
    private val _nasUiChecking = MutableStateFlow(false)

    val bridgeTestStatus: StateFlow<ConnectionTestStatus> = combineTestStatus(
        urlSelector = { it.bridgeUrl },
        resultFlow = _bridgeTestResult,
        checkingFlow = _bridgeChecking,
    )

    val meTubeTestStatus: StateFlow<ConnectionTestStatus> = combineTestStatus(
        urlSelector = { it.meTubeUrl },
        resultFlow = _meTubeTestResult,
        checkingFlow = _meTubeChecking,
    )

    val slskdTestStatus: StateFlow<ConnectionTestStatus> = combineTestStatus(
        urlSelector = { it.slskdUrl },
        resultFlow = _slskdTestResult,
        checkingFlow = _slskdChecking,
    )

    val nasUiTestStatus: StateFlow<ConnectionTestStatus> = combineTestStatus(
        urlSelector = { it.nasUiUrl },
        resultFlow = _nasUiTestResult,
        checkingFlow = _nasUiChecking,
    )

    fun setTheme(themeId: ThemeId) {
        viewModelScope.launch { settingsRepository.setTheme(themeId) }
    }

    fun setDapMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDapMode(enabled) }
    }

    fun setShowMiniPlayerBar(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowMiniPlayerBar(enabled) }
    }

    fun setNowPlayingLayoutMode(mode: NowPlayingLayoutMode) {
        viewModelScope.launch { settingsRepository.setNowPlayingLayoutMode(mode) }
    }

    fun setNowPlayingVisualizerMode(mode: NowPlayingVisualizerMode) {
        viewModelScope.launch { settingsRepository.setNowPlayingVisualizerMode(mode) }
    }

    fun setReactiveVisualizerEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setReactiveVisualizerEnabled(enabled) }
    }

    fun cycleNowPlayingVisualizerMode(current: NowPlayingVisualizerMode) {
        val next = NowPlayingVisualizerMode.entries[
            (NowPlayingVisualizerMode.entries.indexOf(current) + 1) % NowPlayingVisualizerMode.entries.size
        ]
        setNowPlayingVisualizerMode(next)
    }

    fun clearValidationError() {
        _urlValidationError.value = null
    }

    fun saveNasConnectionSettings(settings: NasConnectionSettings): Boolean {
        val validationError = validateSettings(settings)
        if (validationError != null) {
            _urlValidationError.value = validationError
            return false
        }
        _urlValidationError.value = null
        viewModelScope.launch {
            settingsRepository.saveNasConnectionSettings(normalizeSettings(settings))
            clearTestResults()
        }
        return true
    }

    fun resetToLocalDefaults() {
        _urlValidationError.value = null
        viewModelScope.launch {
            settingsRepository.saveNasConnectionSettings(LocalServiceDefaults.asSettings())
            clearTestResults()
        }
    }

    fun saveServiceUrl(
        current: NasConnectionSettings,
        meTubeUrl: String? = null,
        slskdUrl: String? = null,
        nasUiUrl: String? = null,
    ): Boolean {
        return saveNasConnectionSettings(
            current.copy(
                meTubeUrl = meTubeUrl ?: current.meTubeUrl,
                slskdUrl = slskdUrl ?: current.slskdUrl,
                nasUiUrl = nasUiUrl ?: current.nasUiUrl,
            ),
        )
    }

    fun testBridgeConnection() {
        val settings = nasConnectionSettings.value
        if (settings.bridgeUrl.isBlank()) return
        viewModelScope.launch {
            _bridgeChecking.value = true
            _bridgeTestResult.value = ServiceUrlTester.testGet(
                url = settings.bridgeUrl,
                bearerToken = settings.bridgeToken.takeIf { it.isNotBlank() },
            )
            _bridgeChecking.value = false
        }
    }

    fun testMeTubeConnection() {
        testServiceUrl(nasConnectionSettings.value.meTubeUrl, _meTubeChecking, _meTubeTestResult)
    }

    fun testSlskdConnection() {
        testServiceUrl(nasConnectionSettings.value.slskdUrl, _slskdChecking, _slskdTestResult)
    }

    fun testNasUiConnection() {
        testServiceUrl(nasConnectionSettings.value.nasUiUrl, _nasUiChecking, _nasUiTestResult)
    }

    fun testAllConnections() {
        testMeTubeConnection()
        testSlskdConnection()
        testNasUiConnection()
    }

    private fun testServiceUrl(
        url: String,
        checkingFlow: MutableStateFlow<Boolean>,
        resultFlow: MutableStateFlow<Result<Int>?>,
    ) {
        if (url.isBlank()) return
        viewModelScope.launch {
            checkingFlow.value = true
            resultFlow.value = ServiceUrlTester.testGet(url)
            checkingFlow.value = false
        }
    }

    private fun clearTestResults() {
        _bridgeTestResult.value = null
        _meTubeTestResult.value = null
        _slskdTestResult.value = null
        _nasUiTestResult.value = null
    }

    private fun combineTestStatus(
        urlSelector: (NasConnectionSettings) -> String,
        resultFlow: MutableStateFlow<Result<Int>?>,
        checkingFlow: MutableStateFlow<Boolean>,
    ): StateFlow<ConnectionTestStatus> {
        return combine(
            nasConnectionSettings,
            resultFlow,
            checkingFlow,
        ) { settings, result, checking ->
            ServiceUrlTester.statusForConfiguredUrl(
                url = urlSelector(settings),
                lastResult = result,
                isChecking = checking,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConnectionTestStatus.NOT_CONFIGURED,
        )
    }

    private fun normalizeSettings(settings: NasConnectionSettings): NasConnectionSettings {
        return settings.copy(
            nasDisplayName = settings.nasDisplayName.trim(),
            bridgeUrl = normalizeOptionalUrl(settings.bridgeUrl),
            bridgeToken = settings.bridgeToken.trim(),
            meTubeUrl = normalizeOptionalUrl(settings.meTubeUrl),
            slskdUrl = normalizeOptionalUrl(settings.slskdUrl),
            nasUiUrl = normalizeOptionalUrl(settings.nasUiUrl),
        )
    }

    private fun normalizeOptionalUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return ""
        return ServiceUrlValidator.normalize(trimmed)
    }

    private fun validateSettings(settings: NasConnectionSettings): String? {
        val urls = listOf(
            "MeTube URL" to settings.meTubeUrl,
            "slskd URL" to settings.slskdUrl,
            "NAS UI URL" to settings.nasUiUrl,
            "Bridge URL" to settings.bridgeUrl,
        )
        urls.forEach { (label, url) ->
            if (url.isNotBlank()) {
                ServiceUrlValidator.validate(url).onFailure { error ->
                    return "$label: ${error.message}"
                }
            }
        }
        return null
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
