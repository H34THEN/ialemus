package com.heathen.ialemus.core.spotify

import android.content.Context
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Connects to the Spotify app on the same device via Spotify App Remote SDK.
 * SPOTIFY REMOTE only — does not stream audio through Ialemus ExoPlayer.
 */
class SpotifyRemoteRepository(
    private val context: Context,
) {
    private var appRemote: SpotifyAppRemote? = null

    private val _connectionState =
        MutableStateFlow(SpotifyRemoteConnectionState.DISCONNECTED)
    val connectionState: StateFlow<SpotifyRemoteConnectionState> = _connectionState.asStateFlow()

    private val _playerState = MutableStateFlow<SpotifyRemotePlayerState?>(null)
    val playerState: StateFlow<SpotifyRemotePlayerState?> = _playerState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun connect(clientId: String, redirectUri: String) {
        if (_connectionState.value == SpotifyRemoteConnectionState.CONNECTING) return
        if (_connectionState.value == SpotifyRemoteConnectionState.CONNECTED && appRemote != null) return

        _connectionState.value = SpotifyRemoteConnectionState.CONNECTING
        _errorMessage.value = null

        val params = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            context,
            params,
            object : Connector.ConnectionListener {
                override fun onConnected(remote: SpotifyAppRemote) {
                    appRemote = remote
                    _connectionState.value = SpotifyRemoteConnectionState.CONNECTED
                    _errorMessage.value = null
                    remote.playerApi.subscribeToPlayerState().setEventCallback { state ->
                        _playerState.value = mapPlayerState(state)
                    }
                }

                override fun onFailure(error: Throwable) {
                    appRemote = null
                    _connectionState.value = SpotifyRemoteConnectionState.ERROR
                    _errorMessage.value = error.message ?: "Spotify Remote connection failed"
                }
            },
        )
    }

    fun disconnect() {
        SpotifyAppRemote.disconnect(appRemote)
        appRemote = null
        _connectionState.value = SpotifyRemoteConnectionState.DISCONNECTED
        _playerState.value = null
        _errorMessage.value = null
    }

    fun resume(): Result<Unit> = runRemoteCommand { it.playerApi.resume() }
    fun pause(): Result<Unit> = runRemoteCommand { it.playerApi.pause() }
    fun skipNext(): Result<Unit> = runRemoteCommand { it.playerApi.skipNext() }
    fun skipPrevious(): Result<Unit> = runRemoteCommand { it.playerApi.skipPrevious() }
    fun playUri(uri: String): Result<Unit> = runRemoteCommand { it.playerApi.play(uri) }

    private inline fun runRemoteCommand(block: (SpotifyAppRemote) -> Unit): Result<Unit> {
        val remote = appRemote
        if (remote == null || _connectionState.value != SpotifyRemoteConnectionState.CONNECTED) {
            return Result.failure(IllegalStateException("Spotify Remote not connected"))
        }
        return try {
            block(remote)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun mapPlayerState(state: PlayerState): SpotifyRemotePlayerState {
        val track = state.track
        val artistName = track.artist.name.orEmpty().ifBlank { "Unknown artist" }
        return SpotifyRemotePlayerState(
            trackName = track.name.orEmpty().ifBlank { "Unknown track" },
            artistName = artistName,
            albumName = track.album.name.orEmpty().ifBlank { "Unknown album" },
            imageUri = track.imageUri?.raw,
            isPaused = state.isPaused,
            playbackPositionMs = state.playbackPosition,
            trackDurationMs = track.duration,
        )
    }
}
