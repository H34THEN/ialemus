package com.heathen.ialemus.core.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.heathen.ialemus.BuildConfig
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.widget.IalemusPlaybackWidgetProvider
import com.heathen.ialemus.widget.WidgetStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

private const val TAG = "PlayerConnection"
private const val PLAYBACK_ERROR_MESSAGE = "Playback link failed. Try rescanning this source."

class PlayerConnection(
    private val context: Context,
    private val queueRepository: QueueRepository,
    private val shuffleEngine: ShuffleEngine,
    private val widgetStateStore: WidgetStateStore? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controller: MediaController? = null
    private var lastWidgetTitle: String? = null
    private var lastWidgetPlaying: Boolean? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateFromPlayer()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateFromPlayer()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: -1
            queueRepository.updateCurrentIndex(index)
            updateFromPlayer()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            updateFromPlayer()
        }
    }

    suspend fun connect() {
        if (controller != null) return
        val sessionToken = SessionToken(
            context,
            ComponentName(context, IalemusPlaybackService::class.java),
        )
        controller = MediaController.Builder(context, sessionToken).buildAsync().await()
        controller?.addListener(playerListener)
        _playbackState.update { it.copy(isConnected = true) }
        updateFromPlayer()
        startPositionUpdates()
    }

    fun disconnect() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
        scope.cancel()
        _playbackState.update { it.copy(isConnected = false) }
    }

    fun clearPlaybackError() {
        _playbackState.update { it.copy(playbackError = null) }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int, mode: ShuffleMode = queueRepository.shuffleMode.value) {
        if (tracks.isEmpty()) {
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
            return
        }
        val safeStart = startIndex.coerceIn(0, tracks.lastIndex)
        val selectedTrack = tracks[safeStart]
        val playerStartIndex = queueRepository.setQueue(tracks, safeStart, mode)
        val queue = queueRepository.activeQueue.value
        val mediaController = controller
        if (mediaController == null) {
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
            return
        }

        try {
            PlaybackIndexMapper.logSelection(
                selectedTrack = selectedTrack,
                listIndex = safeStart,
                playerStartIndex = playerStartIndex,
                queueSize = queue.size,
            )

            mediaController.shuffleModeEnabled = false
            mediaController.repeatMode = shuffleEngine.playerRepeatMode(mode)
            mediaController.setMediaItems(queue.toMediaItems(), playerStartIndex, 0L)
            mediaController.prepare()
            mediaController.play()
            clearPlaybackError()
            updateFromPlayer()
        } catch (error: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "playTracks failed for trackId=${selectedTrack.id}", error)
            }
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
        }
    }

    fun playTrackById(tracks: List<Track>, trackId: String, mode: ShuffleMode = queueRepository.shuffleMode.value) {
        val startIndex = PlaybackIndexMapper.resolveStartIndex(tracks, trackId)
        if (startIndex == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "playTrackById could not resolve trackId=$trackId")
            }
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
            return
        }
        playTracks(tracks, startIndex, mode)
    }

    fun playPause() {
        val mediaController = controller ?: return
        if (mediaController.isPlaying) {
            mediaController.pause()
        } else {
            mediaController.play()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        updateFromPlayer()
    }

    fun skipToPrevious() {
        controller?.seekToPreviousMediaItem()
        updateFromPlayer()
    }

    fun skipToNext() {
        controller?.seekToNextMediaItem()
        updateFromPlayer()
    }

    fun playQueueItem(index: Int) {
        val queue = queueRepository.activeQueue.value
        val track = queue.getOrNull(index) ?: return
        controller?.seekToDefaultPosition(index)
        controller?.play()
        queueRepository.updateCurrentIndex(index)
        PlaybackIndexMapper.logSelection(
            selectedTrack = track,
            listIndex = index,
            playerStartIndex = index,
            queueSize = queue.size,
        )
        updateFromPlayer()
    }

    fun setShuffleMode(mode: ShuffleMode) {
        queueRepository.setShuffleMode(mode)
        val current = queueRepository.currentTrack() ?: return
        playTrackById(queueRepository.activeQueue.value, current.id, mode)
    }

    private fun startPositionUpdates() {
        scope.launch {
            while (controller != null) {
                updateFromPlayer()
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun updateFromPlayer() {
        val mediaController = controller
        val queue = queueRepository.activeQueue.value
        val playerIndex = mediaController?.currentMediaItemIndex ?: -1
        if (playerIndex >= 0) {
            queueRepository.updateCurrentIndex(playerIndex)
        }
        val currentTrack = queue.getOrNull(playerIndex)
            ?: mediaController?.currentMediaItem?.mediaId?.let { mediaId ->
                queue.find { it.id == mediaId }
            }
        _playbackState.update { previous ->
            previous.copy(
                currentTrack = currentTrack,
                isPlaying = mediaController?.isPlaying == true,
                isBuffering = mediaController?.playbackState == Player.STATE_BUFFERING,
                positionMs = mediaController?.currentPosition ?: 0L,
                durationMs = mediaController?.duration?.takeIf { value -> value > 0 }
                    ?: currentTrack?.durationMs
                    ?: 0L,
                queueIndex = playerIndex,
                queueSize = queue.size,
                isConnected = mediaController != null,
            )
        }
        syncWidget(
            track = currentTrack,
            isPlaying = mediaController?.isPlaying == true,
        )
    }

    private fun syncWidget(track: Track?, isPlaying: Boolean) {
        val store = widgetStateStore ?: return
        val title = track?.title
        if (title == lastWidgetTitle && isPlaying == lastWidgetPlaying) return
        lastWidgetTitle = title
        lastWidgetPlaying = isPlaying
        store.update(title, track?.displayArtist, isPlaying)
        IalemusPlaybackWidgetProvider.refreshAll(context)
    }

    private fun setPlaybackError(message: String) {
        _playbackState.update { it.copy(playbackError = message) }
    }
}
