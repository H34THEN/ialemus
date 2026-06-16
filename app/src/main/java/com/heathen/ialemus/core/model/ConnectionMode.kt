package com.heathen.ialemus.core.model

enum class ConnectionMode(val displayName: String) {
    LOCAL_LAN("Local LAN"),
    VPN_TAILSCALE("VPN / Tailscale"),
    MANUAL_URL("Manual URL"),
}
