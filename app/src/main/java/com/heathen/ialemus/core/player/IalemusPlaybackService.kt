package com.heathen.ialemus.core.player

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Started by Media3 when [MediaController] connects — not via [android.content.Context.startForegroundService].
 * Foreground promotion is handled by MediaSessionService when playback requires a notification.
 */
class IalemusPlaybackService : MediaSessionService() {
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        player = exoPlayer
        PlaybackAudioSessionHolder.update(exoPlayer.audioSessionId)
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
        // TODO: ReplayGain, lyrics, output device info, persistent queue restore, Android Auto.
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        PlaybackAudioSessionHolder.clear()
        mediaSession?.release()
        player?.release()
        mediaSession = null
        player = null
        super.onDestroy()
    }
}
