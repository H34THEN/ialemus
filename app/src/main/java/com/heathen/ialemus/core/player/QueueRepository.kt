package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QueueRepository(
    private val shuffleEngine: ShuffleEngine,
) {
    private val _shuffleMode = MutableStateFlow(ShuffleMode.DEFAULT)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    private val _shuffleSeed = MutableStateFlow(System.currentTimeMillis())
    val shuffleSeed: StateFlow<Long> = _shuffleSeed.asStateFlow()

    private val _originalQueue = MutableStateFlow<List<Track>>(emptyList())
    private val _activeQueue = MutableStateFlow<List<Track>>(emptyList())
    val activeQueue: StateFlow<List<Track>> = _activeQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    /**
     * Builds the active queue and returns the index ExoPlayer must start at.
     */
    fun setQueue(
        tracks: List<Track>,
        startIndex: Int,
        mode: ShuffleMode = _shuffleMode.value,
        reshuffle: Boolean = false,
    ): Int {
        if (tracks.isEmpty()) {
            clear()
            return 0
        }
        val safeStart = startIndex.coerceIn(0, tracks.lastIndex)
        val startTrack = tracks[safeStart]
        if (reshuffle || mode != _shuffleMode.value) {
            _shuffleSeed.value = System.currentTimeMillis()
        }
        _shuffleMode.value = mode
        _originalQueue.value = tracks
        val ordered = shuffleEngine.orderQueue(
            tracks = tracks,
            mode = mode,
            seed = _shuffleSeed.value,
            startTrackId = startTrack.id,
        )
        _activeQueue.value = ordered
        val playerStartIndex = PlaybackIndexMapper.resolveStartIndexInQueue(ordered, startTrack.id)
        _currentIndex.value = playerStartIndex
        return playerStartIndex
    }

    fun updateCurrentIndex(index: Int) {
        if (index in _activeQueue.value.indices) {
            _currentIndex.value = index
        }
    }

    fun currentTrack(): Track? = _activeQueue.value.getOrNull(_currentIndex.value)

    fun setShuffleMode(mode: ShuffleMode) {
        val current = currentTrack() ?: run {
            _shuffleMode.value = mode
            return
        }
        setQueue(
            tracks = _originalQueue.value,
            startIndex = _originalQueue.value.indexOfFirst { it.id == current.id }.coerceAtLeast(0),
            mode = mode,
            reshuffle = true,
        )
    }

    fun clear() {
        _originalQueue.value = emptyList()
        _activeQueue.value = emptyList()
        _currentIndex.value = -1
    }
}
