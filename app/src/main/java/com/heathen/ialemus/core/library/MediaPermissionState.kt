package com.heathen.ialemus.core.library

sealed interface MediaPermissionState {
    data object Granted : MediaPermissionState
    data object NotGranted : MediaPermissionState
    data object Denied : MediaPermissionState
    data object DeniedPermanently : MediaPermissionState
    data object Unknown : MediaPermissionState
}
