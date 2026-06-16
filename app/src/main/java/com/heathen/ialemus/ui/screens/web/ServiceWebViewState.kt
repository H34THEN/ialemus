package com.heathen.ialemus.ui.screens.web

data class ServiceWebViewState(
    val serviceName: String,
    val url: String,
    val serviceKind: DockerWebService? = null,
)

enum class DockerWebService(val displayName: String) {
    METUBE("MeTube"),
    SLSKD("slskd"),
    NAS_UI("Ugreen NAS"),
}
