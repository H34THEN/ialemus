package com.heathen.ialemus.core.visualizer

data class AudioVisualizerState(
    val barLevels: List<Float> = List(BAR_COUNT) { 0.1f },
    val waveform: List<Float> = emptyList(),
    val isReactiveAudio: Boolean = false,
    val fallbackReason: String? = "Simulated signal",
    val permissionGranted: Boolean = false,
    val reactiveEnabled: Boolean = false,
    val isAttached: Boolean = false,
) {
    companion object {
        const val BAR_COUNT = 16
        const val WAVEFORM_POINTS = 48
    }
}
