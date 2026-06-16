package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.SourceType
import com.heathen.ialemus.core.model.Track
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackIndexMapperTest {
    private val tracks = listOf(
        track("a", "Alpha"),
        track("b", "Beta"),
        track("c", "Gamma"),
    )

    @Test
    fun resolveStartIndex_findsSelectedTrack() {
        assertEquals(1, PlaybackIndexMapper.resolveStartIndex(tracks, "b"))
        assertEquals(2, PlaybackIndexMapper.resolveStartIndex(tracks, "c"))
    }

    @Test
    fun resolveStartIndexInQueue_mapsAfterShuffleRotation() {
        val shuffled = listOf(tracks[2], tracks[0], tracks[1])
        assertEquals(0, PlaybackIndexMapper.resolveStartIndexInQueue(shuffled, "c"))
        assertEquals(1, PlaybackIndexMapper.resolveStartIndexInQueue(shuffled, "a"))
    }

    private fun track(id: String, title: String) = Track(
        id = id,
        mediaStoreId = -1,
        title = title,
        contentUri = "content://test/$id",
        sourceType = SourceType.SAF_FOLDER,
    )
}
