package com.heathen.ialemus.core.lyrics

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.heathen.ialemus.core.diagnostics.StabilityDiagnostics
import com.heathen.ialemus.core.model.SourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.data.local.entity.LyricsSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class SidecarLyricsResult(
    val rawText: String,
    val sourceType: LyricsSourceType,
    val isSynced: Boolean,
)

class LyricsScanner(
    private val context: Context,
) {
    suspend fun scanSidecarForTrack(track: Track, treeUri: String?): SidecarLyricsResult? = withContext(Dispatchers.IO) {
        StabilityDiagnostics.metadataExtractionStart(1)
        val start = System.currentTimeMillis()
        val result = findSidecar(track, treeUri)
        StabilityDiagnostics.metadataExtractionEnd(System.currentTimeMillis() - start)
        result
    }

    suspend fun readUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            }
        }.getOrNull()
    }

    suspend fun extractEmbeddedLyrics(track: Track): SidecarLyricsResult? = withContext(Dispatchers.IO) {
        // TODO: embedded lyric tags vary by codec/container; sidecar/manual import is the reliable MVP path.
        null
    }

    private fun findSidecar(track: Track, treeUri: String?): SidecarLyricsResult? {
        val contentUri = Uri.parse(track.contentUri)
        val fileName = contentUri.lastPathSegment ?: return null
        val baseName = fileName.substringBeforeLast('.')
        if (baseName.isBlank()) return null

        val candidates = buildList {
            add("$baseName.lrc")
            add("$baseName.LRC")
            add("$baseName.txt")
            add("$baseName.TXT")
            add("${track.displayArtist} - ${track.displayTitle}.lrc")
            add("${track.displayTitle}.lrc")
        }.distinct()

        if (track.sourceType == SourceType.SAF_FOLDER && treeUri != null) {
            val tree = DocumentFile.fromTreeUri(context, Uri.parse(treeUri)) ?: return null
            val audioDoc = DocumentFile.fromSingleUri(context, contentUri)
            val parent = audioDoc?.parentFile ?: tree
            candidates.forEach { name ->
                parent.findFile(name)?.let { doc ->
                    if (doc.isFile) {
                        return readSidecarDoc(doc, name)
                    }
                }
            }
        }

        // Best-effort: try sibling URIs in same folder via display name only when parent is known.
        return null
    }

    private fun readSidecarDoc(doc: DocumentFile, name: String): SidecarLyricsResult? {
        val text = runCatching {
            context.contentResolver.openInputStream(doc.uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            }
        }.getOrNull()?.trim().orEmpty()
        if (text.isBlank()) return null
        val isLrc = name.endsWith(".lrc", ignoreCase = true)
        val parsed = LyricsParser.parse(text)
        return SidecarLyricsResult(
            rawText = text,
            sourceType = if (isLrc) LyricsSourceType.LRC_SIDECAR else LyricsSourceType.TXT_SIDECAR,
            isSynced = parsed.isSynced,
        )
    }
}
