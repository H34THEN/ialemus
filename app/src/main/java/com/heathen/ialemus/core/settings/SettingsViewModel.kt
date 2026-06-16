package com.heathen.ialemus.core.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.network.ServiceUrlTester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val nasConnectionSettings: StateFlow<NasConnectionSettings> =
        settingsRepository.nasConnectionSettings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NasConnectionSettings(),
        )

    private val _bridgeTestResult = MutableStateFlow<Result<Int>?>(null)
    private val _meTubeTestResult = MutableStateFlow<Result<Int>?>(null)
    private val _slskdTestResult = MutableStateFlow<Result<Int>?>(null)
    private val _bridgeChecking = MutableStateFlow(false)
    private val _meTubeChecking = MutableStateFlow(false)
    private val _slskdChecking = MutableStateFlow(false)

    val bridgeTestStatus: StateFlow<ConnectionTestStatus> = combineTestStatus(
        urlFlow = nasConnectionSettings,
        urlSelector = { it.bridgeUrl },
        resultFlow = _bridgeTestResult,
        checkingFlow = _bridgeChecking,
    )

    val meTubeTestStatus: StateFlow<ConnectionTestStatus> = combineTestStatus(
        urlFlow = nasConnectionSettings,
        urlSelector = { it.meTubeUrl },
        resultFlow = _meTubeTestResult,
        checkingFlow = _meTubeChecking,
    )

    val slskdTestStatus: StateFlow<ConnectionTestStatus> = combineTestStatus(
        urlFlow = nasConnectionSettings,
        urlSelector = { it.slskdUrl },
        resultFlow = _slskdTestResult,
        checkingFlow = _slskdChecking,
    )

    fun setTheme(themeId: ThemeId) {
        viewModelScope.launch { settingsRepository.setTheme(themeId) }
    }

    fun setDapMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDapMode(enabled) }
    }

    fun saveNasConnectionSettings(settings: NasConnectionSettings) {
        viewModelScope.launch {
            settingsRepository.saveNasConnectionSettings(settings)
            _bridgeTestResult.value = null
            _meTubeTestResult.value = null
            _slskdTestResult.value = null
        }
    }

    fun saveServiceUrl(
        current: NasConnectionSettings,
        meTubeUrl: String? = null,
        slskdUrl: String? = null,
    ) {
        saveNasConnectionSettings(
            current.copy(
                meTubeUrl = meTubeUrl ?: current.meTubeUrl,
                slskdUrl = slskdUrl ?: current.slskdUrl,
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
        val url = nasConnectionSettings.value.meTubeUrl
        if (url.isBlank()) return
        viewModelScope.launch {
            _meTubeChecking.value = true
            _meTubeTestResult.value = ServiceUrlTester.testGet(url)
            _meTubeChecking.value = false
        }
    }

    fun testSlskdConnection() {
        val url = nasConnectionSettings.value.slskdUrl
        if (url.isBlank()) return
        viewModelScope.launch {
            _slskdChecking.value = true
            _slskdTestResult.value = ServiceUrlTester.testGet(url)
            _slskdChecking.value = false
        }
    }

    fun testAllConnections() {
        testBridgeConnection()
        testMeTubeConnection()
        testSlskdConnection()
    }

    private fun combineTestStatus(
        urlFlow: StateFlow<NasConnectionSettings>,
        urlSelector: (NasConnectionSettings) -> String,
        resultFlow: MutableStateFlow<Result<Int>?>,
        checkingFlow: MutableStateFlow<Boolean>,
    ): StateFlow<ConnectionTestStatus> {
        return kotlinx.coroutines.flow.combine(
            urlFlow,
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
