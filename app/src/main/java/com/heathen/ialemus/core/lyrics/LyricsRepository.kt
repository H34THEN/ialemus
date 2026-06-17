package com.heathen.ialemus.core.lyrics

import android.net.Uri
import com.heathen.ialemus.core.diagnostics.StabilityDiagnostics
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.data.local.dao.LyricsDao
import com.heathen.ialemus.data.local.entity.LyricsEntity
import com.heathen.ialemus.data.local.entity.LyricsSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LyricsRepository(
    private val lyricsDao: LyricsDao,
    private val lyricsScanner: LyricsScanner,
) {
    fun observeLyrics(trackId: String): Flow<LyricsEntity?> = lyricsDao.observeByTrackId(trackId)

    fun observeTrackIdsWithLyrics(): Flow<List<String>> = lyricsDao.observeTrackIdsWithLyrics()

    suspend fun saveManualLyrics(trackId: String, rawText: String) = withContext(Dispatchers.IO) {
        StabilityDiagnostics.lyricsParseStart(trackId)
        val parsed = LyricsParser.parse(rawText)
        StabilityDiagnostics.lyricsParseEnd(parsed.isSynced, parsed.lines.size)
        upsert(trackId, rawText, LyricsSourceType.MANUAL, parsed.isSynced)
    }

    suspend fun importFromUri(trackId: String, uri: Uri) = withContext(Dispatchers.IO) {
        val raw = lyricsScanner.readUri(uri) ?: return@withContext
        val parsed = LyricsParser.parse(raw)
        val source = if (parsed.isSynced) LyricsSourceType.LRC_SIDECAR else LyricsSourceType.TXT_SIDECAR
        upsert(trackId, raw, source, parsed.isSynced)
    }

    suspend fun scanSidecarForTrack(track: Track, treeUri: String?) = withContext(Dispatchers.IO) {
        val sidecar = lyricsScanner.scanSidecarForTrack(track, treeUri) ?: return@withContext false
        upsert(track.id, sidecar.rawText, sidecar.sourceType, sidecar.isSynced)
        true
    }

    suspend fun tryExtractEmbedded(track: Track) = withContext(Dispatchers.IO) {
        val embedded = lyricsScanner.extractEmbeddedLyrics(track) ?: return@withContext false
        upsert(track.id, embedded.rawText, embedded.sourceType, embedded.isSynced)
        true
    }

    suspend fun clearLyrics(trackId: String) = withContext(Dispatchers.IO) {
        lyricsDao.deleteByTrackId(trackId)
    }

    suspend fun getParsed(trackId: String): ParsedLyrics? = withContext(Dispatchers.IO) {
        val entity = lyricsDao.getByTrackId(trackId) ?: return@withContext null
        LyricsParser.parse(entity.rawText)
    }

    private suspend fun upsert(
        trackId: String,
        rawText: String,
        sourceType: LyricsSourceType,
        isSynced: Boolean,
    ) {
        val now = System.currentTimeMillis()
        val existing = lyricsDao.getByTrackId(trackId)
        lyricsDao.upsert(
            LyricsEntity(
                trackId = trackId,
                sourceType = sourceType.name,
                rawText = rawText.trim(),
                isSynced = isSynced,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }
}
