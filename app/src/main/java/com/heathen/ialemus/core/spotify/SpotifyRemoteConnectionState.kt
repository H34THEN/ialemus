package com.heathen.ialemus.core.spotify

enum class SpotifyRemoteConnectionState(val label: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting…"),
    CONNECTED("Connected"),
    ERROR("Error"),
}
