package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.library.withOverrides
import com.heathen.ialemus.data.local.entity.TrackOverrideEntity

fun PlaybackState.withTrackOverrides(overrideMap: Map<String, TrackOverrideEntity>): PlaybackState {
    val current = currentTrack ?: return this
    return copy(currentTrack = current.withOverrides(overrideMap[current.id]))
}
