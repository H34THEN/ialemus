package com.heathen.ialemus.core.player

import androidx.media3.common.C
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Published by [IalemusPlaybackService] when ExoPlayer is ready.
 * Used by the reactive audio visualizer to attach to the playback session.
 */
object PlaybackAudioSessionHolder {
    private val _audioSessionId = MutableStateFlow(C.AUDIO_SESSION_ID_UNSET)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    fun update(sessionId: Int) {
        if (_audioSessionId.value != sessionId) {
            _audioSessionId.value = sessionId
        }
    }

    fun clear() {
        _audioSessionId.value = C.AUDIO_SESSION_ID_UNSET
    }
}
