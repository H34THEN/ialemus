package com.heathen.ialemus.core.diagnostics

import android.util.Log
import com.heathen.ialemus.BuildConfig

private const val TAG = "IalemusStability"

object StabilityDiagnostics {
    fun scanStart(sourceCount: Int) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "scan_start sources=$sourceCount")
    }

    fun scanEnd(trackCount: Int, durationMs: Long) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "scan_end tracks=$trackCount durationMs=$durationMs")
    }

    fun playlistImportStart(name: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "playlist_import_start name=${name.take(40)}")
    }

    fun playlistImportEnd(trackCount: Int) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "playlist_import_end tracks=$trackCount")
    }

    fun metadataExtractionStart(trackCount: Int) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "metadata_extraction_start count=$trackCount")
    }

    fun metadataExtractionEnd(durationMs: Long) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "metadata_extraction_end durationMs=$durationMs")
    }

    fun visualizerModeChanged(mode: String, reactive: Boolean) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "visualizer_mode mode=$mode reactive=$reactive")
    }

    fun playbackTicker(tickMs: Long, playing: Boolean) {
        if (!BuildConfig.DEBUG) return
        // Throttled externally — not every frame.
        Log.v(TAG, "playback_ticker intervalMs=$tickMs playing=$playing")
    }

    fun lyricsParseStart(trackId: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "lyrics_parse_start track=${trackId.take(12)}")
    }

    fun lyricsParseEnd(synced: Boolean, lineCount: Int) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "lyrics_parse_end synced=$synced lines=$lineCount")
    }
}
