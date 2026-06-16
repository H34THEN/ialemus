package com.heathen.ialemus.ui.util

import android.net.Uri
import com.heathen.ialemus.core.model.Track

fun Track.albumArtUri(): Uri? {
    val albumId = albumId ?: return null
    return Uri.parse("content://media/external/audio/albumart/$albumId")
}
