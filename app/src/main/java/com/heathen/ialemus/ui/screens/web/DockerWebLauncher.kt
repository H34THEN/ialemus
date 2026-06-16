package com.heathen.ialemus.ui.screens.web

import com.heathen.ialemus.core.network.ServiceUrlValidator
import com.heathen.ialemus.core.settings.LocalServiceDefaults

fun openDockerServiceInApp(
    service: DockerWebService,
    savedUrl: String,
    onOpen: (String, String) -> Unit,
) {
    val raw = savedUrl.ifBlank { defaultUrlFor(service) }
    val url = ServiceUrlValidator.normalizeForLoad(raw)
    if (url.isBlank()) return
    onOpen(service.displayName, url)
}

fun defaultUrlFor(service: DockerWebService): String = when (service) {
    DockerWebService.METUBE -> LocalServiceDefaults.METUBE
    DockerWebService.SLSKD -> LocalServiceDefaults.SLSKD
    DockerWebService.NAS_UI -> LocalServiceDefaults.NAS_UI
}
