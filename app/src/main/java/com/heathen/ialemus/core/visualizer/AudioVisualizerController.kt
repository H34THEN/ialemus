package com.heathen.ialemus.core.visualizer

import android.media.audiofx.Visualizer
import android.util.Log
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlaybackAudioSessionHolder
import com.heathen.ialemus.core.player.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

private const val TAG = "AudioVisualizer"
private const val SIM_TICK_MS = 80L
private const val BAR_COUNT = AudioVisualizerState.BAR_COUNT

class AudioVisualizerController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var visualizer: Visualizer? = null
    private var simJob: Job? = null
    private var attachedSessionId: Int = -1
    private var reactiveRequested = false
    private var permissionGranted = false
    private var simSeed = 42
    private var simPhase = 0f

    private val _state = MutableStateFlow(AudioVisualizerState())
    val state: StateFlow<AudioVisualizerState> = _state.asStateFlow()

    fun setReactiveEnabled(enabled: Boolean, permissionGranted: Boolean) {
        reactiveRequested = enabled
        this.permissionGranted = permissionGranted
        _state.update {
            it.copy(
                reactiveEnabled = enabled,
                permissionGranted = permissionGranted,
            )
        }
        if (!enabled) {
            releaseVisualizer()
            _state.update {
                it.copy(isReactiveAudio = false, fallbackReason = "Simulated signal", isAttached = false)
            }
            stopSimulated()
        } else {
            tryAttach(PlaybackAudioSessionHolder.audioSessionId.value)
        }
    }

    fun onPlaybackChanged(playbackState: PlaybackState, track: Track?) {
        if (!playbackState.isPlaying) {
            publishIdle()
            return
        }
        if (reactiveRequested && permissionGranted) {
            tryAttach(PlaybackAudioSessionHolder.audioSessionId.value)
        }
        if (!_state.value.isReactiveAudio) {
            startSimulated(playbackState, track)
        }
    }

    fun onAudioSessionChanged(sessionId: Int) {
        if (reactiveRequested && permissionGranted) {
            tryAttach(sessionId)
        }
    }

    fun release() {
        stopSimulated()
        releaseVisualizer()
        _state.value = AudioVisualizerState()
    }

    private fun tryAttach(sessionId: Int) {
        if (!reactiveRequested || !permissionGranted) return
        if (sessionId <= 0) {
            _state.update {
                it.copy(
                    isReactiveAudio = false,
                    fallbackReason = "No audio session — simulated signal",
                    isAttached = false,
                )
            }
            return
        }
        if (attachedSessionId == sessionId && visualizer != null) return
        releaseVisualizer()
        try {
            val viz = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                scalingMode = Visualizer.SCALING_MODE_NORMALIZED
                measurementMode = Visualizer.MEASUREMENT_MODE_PEAK_RMS
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int,
                        ) {
                            if (waveform == null) return
                            val floats = waveform.map { (it.toInt() and 0xFF) / 128f - 1f }
                            _state.update { prev ->
                                prev.copy(
                                    waveform = floats.take(AudioVisualizerState.WAVEFORM_POINTS),
                                    isReactiveAudio = true,
                                    fallbackReason = null,
                                    isAttached = true,
                                )
                            }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int,
                        ) {
                            if (fft == null || fft.size < 8) return
                            val bars = fftToBars(fft)
                            _state.update { prev ->
                                prev.copy(
                                    barLevels = bars,
                                    isReactiveAudio = true,
                                    fallbackReason = null,
                                    isAttached = true,
                                )
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    true,
                )
                enabled = true
            }
            visualizer = viz
            attachedSessionId = sessionId
            stopSimulated()
            _state.update {
                it.copy(
                    isReactiveAudio = true,
                    fallbackReason = null,
                    isAttached = true,
                )
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "Attached reactive visualizer session=$sessionId")
        } catch (error: SecurityException) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Visualizer permission denied", error)
            _state.update {
                it.copy(
                    isReactiveAudio = false,
                    fallbackReason = "Permission denied — simulated signal",
                    isAttached = false,
                )
            }
            releaseVisualizer()
        } catch (error: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Visualizer attach failed", error)
            _state.update {
                it.copy(
                    isReactiveAudio = false,
                    fallbackReason = "Visualizer unavailable — simulated signal",
                    isAttached = false,
                )
            }
            releaseVisualizer()
        }
    }

    private fun releaseVisualizer() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {
        }
        visualizer = null
        attachedSessionId = -1
    }

    private fun startSimulated(playbackState: PlaybackState, track: Track?) {
        if (_state.value.isReactiveAudio) return
        simSeed = track?.id?.hashCode() ?: playbackState.currentTrack?.id?.hashCode() ?: 42
        if (simJob?.isActive == true) return
        val random = Random(simSeed)
        simJob = scope.launch {
            while (isActive) {
                if (_state.value.isReactiveAudio) break
                simPhase += 0.08f + (playbackState.positionMs % 1000L) / 50_000f
                val bars = List(BAR_COUNT) { index ->
                    val wave = sin(simPhase + index * 0.55f + random.nextFloat() * 0.2f).toFloat()
                    (0.2f + 0.7f * ((wave + 1f) / 2f)).coerceIn(0.05f, 1f)
                }
                val wavePoints = List(AudioVisualizerState.WAVEFORM_POINTS) { i ->
                    sin(simPhase * 1.4f + i * 0.28f).toFloat() * 0.85f
                }
                _state.update { prev ->
                    if (prev.isReactiveAudio) prev else prev.copy(
                        barLevels = bars,
                        waveform = wavePoints,
                        fallbackReason = prev.fallbackReason ?: "Simulated signal",
                        isReactiveAudio = false,
                    )
                }
                delay(SIM_TICK_MS)
            }
        }
    }

    private fun stopSimulated() {
        simJob?.cancel()
        simJob = null
    }

    private fun publishIdle() {
        if (_state.value.isReactiveAudio) return
        stopSimulated()
        _state.update { prev ->
            prev.copy(
                barLevels = List(BAR_COUNT) { 0.08f },
                waveform = List(AudioVisualizerState.WAVEFORM_POINTS) { 0f },
            )
        }
    }

    private fun fftToBars(fft: ByteArray): List<Float> {
        val bars = MutableList(BAR_COUNT) { 0.1f }
        val chunk = (fft.size / 2 / BAR_COUNT).coerceAtLeast(1)
        for (i in 0 until BAR_COUNT) {
            var sum = 0f
            val start = 2 + i * chunk
            val end = (start + chunk).coerceAtMost(fft.size)
            for (j in start until end step 2) {
                val real = fft[j].toInt()
                val imag = if (j + 1 < fft.size) fft[j + 1].toInt() else 0
                sum += kotlin.math.sqrt((real * real + imag * imag).toFloat())
            }
            bars[i] = (sum / chunk / 128f).coerceIn(0.05f, 1f)
        }
        return bars
    }
}
