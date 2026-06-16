package com.heathen.ialemus.core.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.LibrarySource
import com.heathen.ialemus.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val repository = container.libraryRepository

    val tracks: StateFlow<List<Track>> = repository.tracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val trackCount: StateFlow<Int> = repository.trackCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    val librarySources: StateFlow<List<LibrarySource>> = repository.librarySources.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _permissionState = MutableStateFlow<MediaPermissionState>(MediaPermissionState.Unknown)
    val permissionState: StateFlow<MediaPermissionState> = _permissionState.asStateFlow()

    private val _scanState = MutableStateFlow<LibraryScanState>(LibraryScanState.NoSources)
    val scanState: StateFlow<LibraryScanState> = _scanState.asStateFlow()

    init {
        refreshPermissionState()
        refreshScanState()
    }

    fun refreshPermissionState() {
        _permissionState.value = repository.resolvePermissionState()
    }

    fun refreshScanState() {
        viewModelScope.launch {
            _scanState.value = repository.refreshScanStateHint()
        }
    }

    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        _permissionState.value = when {
            granted -> MediaPermissionState.Granted
            shouldShowRationale -> MediaPermissionState.Denied
            else -> MediaPermissionState.DeniedPermanently
        }
    }

    fun addMusicFolder(uri: Uri, displayName: String) {
        viewModelScope.launch {
            repository.addSafFolder(uri, displayName)
            refreshScanState()
        }
    }

    fun removeMusicFolder(sourceId: String) {
        viewModelScope.launch {
            repository.removeSource(sourceId)
            refreshScanState()
        }
    }

    fun scanSelectedFolders() {
        if (_scanState.value is LibraryScanState.ScanningFolders) return
        viewModelScope.launch {
            _scanState.value = LibraryScanState.ScanningFolders
            when (val result = repository.scanSelectedFolders()) {
                is LibraryScanResult.Success -> {
                    _scanState.value = LibraryScanState.FolderScanComplete(result.trackCount)
                }
                is LibraryScanResult.Error -> {
                    _scanState.value = LibraryScanState.Failed(result.message)
                }
            }
        }
    }

    fun scanFullDeviceLibrary() {
        if (_scanState.value is LibraryScanState.ScanningFullDevice) return
        viewModelScope.launch {
            _scanState.value = LibraryScanState.ScanningFullDevice
            when (val result = repository.scanFullDeviceLibrary()) {
                is LibraryScanResult.Success -> {
                    _scanState.value = LibraryScanState.FullDeviceComplete(result.trackCount)
                }
                is LibraryScanResult.Error -> {
                    _scanState.value = LibraryScanState.Failed(result.message)
                }
            }
        }
    }

    fun requiredPermission(): String = repository.requiredPermission()

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                return LibraryViewModel(container) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
