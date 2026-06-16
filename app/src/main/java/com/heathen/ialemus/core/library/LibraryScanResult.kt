package com.heathen.ialemus.core.library

sealed interface LibraryScanResult {
    data class Success(val trackCount: Int, val sourceLabel: String) : LibraryScanResult
    data class Error(val message: String) : LibraryScanResult
}

sealed interface LibraryScanState {
    data object NoSources : LibraryScanState
    data object Idle : LibraryScanState
    data class FoldersSelected(val count: Int, val notScanned: Boolean = true) : LibraryScanState
    data object ScanningFolders : LibraryScanState
    data class FolderScanComplete(val trackCount: Int) : LibraryScanState
    data object FullDeviceAvailable : LibraryScanState
    data object ScanningFullDevice : LibraryScanState
    data class FullDeviceComplete(val trackCount: Int) : LibraryScanState
    data class Failed(val message: String) : LibraryScanState
}
