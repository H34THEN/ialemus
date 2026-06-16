package com.heathen.ialemus.core.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.heathen.ialemus.core.model.Track

fun Track.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id)
    .setUri(Uri.parse(contentUri))
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(displayArtist)
            .setAlbumTitle(displayAlbum)
            .build(),
    )
    .build()

fun List<Track>.toMediaItems(): List<MediaItem> = map { it.toMediaItem() }
