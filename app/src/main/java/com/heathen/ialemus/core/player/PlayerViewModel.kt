package com.heathen.ialemus.core.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.QueueItem
import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val playerConnection = container.playerConnection
    private val queueRepository = container.queueRepository
    private val trackStatsDao = container.trackStatsDao

    val playbackState: StateFlow<PlaybackState> = playerConnection.playbackState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlaybackState(),
    )

    val queueItems: StateFlow<List<QueueItem>> = combine(
        queueRepository.activeQueue,
        queueRepository.currentIndex,
    ) { queue, index ->
        queue.mapIndexed { itemIndex, track ->
            QueueItem(
                track = track,
                queueIndex = itemIndex,
                isCurrent = itemIndex == index,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val shuffleMode: StateFlow<ShuffleMode> = queueRepository.shuffleMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShuffleMode.DEFAULT,
    )

    fun connect() {
        viewModelScope.launch {
            playerConnection.connect()
        }
    }

    fun disconnect() {
        playerConnection.disconnect()
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        playerConnection.playTracks(tracks, startIndex, queueRepository.shuffleMode.value)
    }

    fun playTrack(tracks: List<Track>, track: Track) {
        playerConnection.playTrackById(tracks, track.id, queueRepository.shuffleMode.value)
    }

    fun playCollection(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        val mode = if (shuffle) ShuffleMode.TRUE_RANDOM else queueRepository.shuffleMode.value
        playerConnection.playTracks(tracks, 0, mode)
    }

    fun playPause() = playerConnection.playPause()

    fun seekTo(positionMs: Long) = playerConnection.seekTo(positionMs)

    fun skipToPrevious() = playerConnection.skipToPrevious()

    fun skipToNext() = playerConnection.skipToNext()

    fun playQueueItem(index: Int) = playerConnection.playQueueItem(index)

    fun setShuffleMode(mode: ShuffleMode) = playerConnection.setShuffleMode(mode)

    fun clearPlaybackError() = playerConnection.clearPlaybackError()

    fun toggleFavorite(trackId: String, favorite: Boolean) {
        viewModelScope.launch {
            trackStatsDao.setFavorite(trackId, favorite)
            // TODO(MVP 1B): Play-count threshold polish and richer favorite persistence.
        }
    }

    fun observeFavorite(trackId: String): Flow<Boolean> =
        trackStatsDao.observeStatsForTrack(trackId).map { stats -> stats?.favorite == true }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(container) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
