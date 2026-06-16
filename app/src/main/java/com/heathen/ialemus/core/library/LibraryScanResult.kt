package com.heathen.ialemus.core.library

sealed interface LibraryScanResult {
    data class Success(val trackCount: Int) : LibraryScanResult
    data class Error(val message: String) : LibraryScanResult
}

sealed interface LibraryScanState {
    data object Idle : LibraryScanState
    data object Scanning : LibraryScanState
    data class Complete(val trackCount: Int) : LibraryScanState
    data class Failed(val message: String) : LibraryScanState
}
