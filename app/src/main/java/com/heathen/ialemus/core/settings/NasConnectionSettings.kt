package com.heathen.ialemus.core.settings

import com.heathen.ialemus.core.model.ConnectionMode

data class NasConnectionSettings(
    val nasDisplayName: String = "",
    val bridgeUrl: String = "",
    val bridgeToken: String = "",
    val meTubeUrl: String = "",
    val slskdUrl: String = "",
    val jellyfinUrl: String = "",
    val connectionMode: ConnectionMode = ConnectionMode.LOCAL_LAN,
) {
    val bridgeConfigured: Boolean
        get() = bridgeUrl.isNotBlank()

    val meTubeConfigured: Boolean
        get() = meTubeUrl.isNotBlank()

    val slskdConfigured: Boolean
        get() = slskdUrl.isNotBlank()
}
