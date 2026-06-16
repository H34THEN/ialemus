package com.heathen.ialemus.core.network

enum class ConnectionTestStatus(val label: String) {
    NOT_CONFIGURED("Not configured"),
    READY("Ready"),
    CHECKING("Checking"),
    REACHABLE("Reachable"),
    FAILED("Failed"),
}
