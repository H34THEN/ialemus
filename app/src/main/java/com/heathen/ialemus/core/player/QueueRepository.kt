package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.RepeatMode
import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QueueRepository(
    private val shuffleEngine: ShuffleEngine,
) {
    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.DEFAULT)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _shuffleSeed = MutableStateFlow(System.currentTimeMillis())
    val shuffleSeed: StateFlow<Long> = _shuffleSeed.asStateFlow()

    private val _originalQueue = MutableStateFlow<List<Track>>(emptyList())
    private val _activeQueue = MutableStateFlow<List<Track>>(emptyList())
    val activeQueue: StateFlow<List<Track>> = _activeQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    /** Legacy combined mode for Now Playing display. */
    fun legacyShuffleMode(): ShuffleMode = when {
        _shuffleEnabled.value -> ShuffleMode.TRUE_RANDOM
        _repeatMode.value == RepeatMode.ONE -> ShuffleMode.REPEAT_ONE
        _repeatMode.value == RepeatMode.QUEUE -> ShuffleMode.REPEAT_QUEUE
        else -> ShuffleMode.OFF
    }

    fun playerRepeatMode(): Int = shuffleEngine.playerRepeatMode(_repeatMode.value)

    /**
     * Builds the active queue and returns the index ExoPlayer must start at.
     */
    fun setQueue(
        tracks: List<Track>,
        startIndex: Int,
        reshuffle: Boolean = false,
    ): Int {
        if (tracks.isEmpty()) {
            clear()
            return 0
        }
        val safeStart = startIndex.coerceIn(0, tracks.lastIndex)
        val startTrack = tracks[safeStart]
        if (reshuffle && _shuffleEnabled.value) {
            _shuffleSeed.value = System.currentTimeMillis()
        }
        _originalQueue.value = tracks
        val ordered = if (_shuffleEnabled.value) {
            shuffleEngine.orderQueue(
                tracks = tracks,
                seed = _shuffleSeed.value,
                startTrackId = startTrack.id,
            )
        } else {
            tracks
        }
        _activeQueue.value = ordered
        val playerStartIndex = PlaybackIndexMapper.resolveStartIndexInQueue(ordered, startTrack.id)
        _currentIndex.value = playerStartIndex.coerceIn(ordered.indices)
        return playerStartIndex
    }

    fun updateCurrentIndex(index: Int) {
        val queue = _activeQueue.value
        if (index in queue.indices) {
            _currentIndex.value = index
        }
    }

    fun currentTrack(): Track? = _activeQueue.value.getOrNull(_currentIndex.value)

    fun toggleShuffle(): Boolean {
        _shuffleEnabled.value = !_shuffleEnabled.value
        rebuildQueue(reshuffle = true)
        return _shuffleEnabled.value
    }

    fun cycleRepeat(): RepeatMode {
        _repeatMode.value = _repeatMode.value.next()
        return _repeatMode.value
    }

    fun setShuffleMode(mode: ShuffleMode) {
        when (mode) {
            ShuffleMode.TRUE_RANDOM -> {
                _shuffleEnabled.value = true
                _repeatMode.value = RepeatMode.OFF
            }
            ShuffleMode.REPEAT_ONE -> {
                _shuffleEnabled.value = false
                _repeatMode.value = RepeatMode.ONE
            }
            ShuffleMode.REPEAT_QUEUE -> {
                _shuffleEnabled.value = false
                _repeatMode.value = RepeatMode.QUEUE
            }
            ShuffleMode.OFF -> {
                _shuffleEnabled.value = false
                _repeatMode.value = RepeatMode.OFF
            }
        }
        rebuildQueue(reshuffle = true)
    }

    fun rebuildQueue(reshuffle: Boolean = false) {
        val original = _originalQueue.value
        if (original.isEmpty()) return
        val current = currentTrack()
        val startIndex = if (current != null) {
            original.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
        } else {
            0
        }
        setQueue(original, startIndex, reshuffle = reshuffle)
    }

    fun clear() {
        _originalQueue.value = emptyList()
        _activeQueue.value = emptyList()
        _currentIndex.value = -1
    }
}
