package com.heathen.ialemus.core.library

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.heathen.ialemus.core.model.LibrarySource
import com.heathen.ialemus.core.model.SourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.model.TrackOrigin
import com.heathen.ialemus.core.model.safTrackId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class SafFolderScanner(
    private val context: Context,
    private val minimumDurationMs: Long = 30_000L,
) {
    suspend fun scanSource(source: LibrarySource): List<Track> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val root = DocumentFile.fromTreeUri(context, Uri.parse(source.treeUri)) ?: return@withContext emptyList()
        val tracks = mutableListOf<Track>()
        crawlFolder(
            folder = root,
            source = source,
            now = now,
            tracks = tracks,
            pathPrefix = source.displayName,
        )
        tracks.sortedBy { it.title.lowercase(Locale.getDefault()) }
    }

    private fun crawlFolder(
        folder: DocumentFile,
        source: LibrarySource,
        now: Long,
        tracks: MutableList<Track>,
        pathPrefix: String,
    ) {
        for (child in folder.listFiles()) {
            if (child.isDirectory) {
                crawlFolder(
                    folder = child,
                    source = source,
                    now = now,
                    tracks = tracks,
                    pathPrefix = "$pathPrefix/${child.name ?: "folder"}",
                )
            } else if (child.isFile && isAudioFile(child)) {
                val uri = child.uri.toString()
                val durationMs = extractDurationMs(child.uri)
                if (durationMs in 1..<minimumDurationMs) continue
                val fileName = child.name ?: "Unknown"
                val title = fileName.substringBeforeLast('.').ifBlank { fileName }
                tracks += Track(
                    id = safTrackId(source.id, uri),
                    mediaStoreId = -1L,
                    title = title,
                    artist = null,
                    album = null,
                    albumId = null,
                    durationMs = durationMs.coerceAtLeast(0L),
                    trackNumber = null,
                    discNumber = null,
                    contentUri = uri,
                    relativePath = pathPrefix,
                    dateAdded = child.lastModified(),
                    dateModified = child.lastModified(),
                    size = child.length(),
                    sourceType = SourceType.SAF_FOLDER,
                    origin = TrackOrigin.MANUAL,
                    librarySourceId = source.id,
                    sourceLabel = source.displayName,
                    lastScannedAt = now,
                )
            }
        }
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        val mime = file.type?.lowercase(Locale.getDefault())
        if (mime != null && mime.startsWith("audio/")) return true
        val extension = file.name?.substringAfterLast('.', "")?.lowercase(Locale.getDefault()).orEmpty()
        return extension in AUDIO_EXTENSIONS
    }

    private fun extractDurationMs(uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            retriever.release()
        }
    }

    companion object {
        private val AUDIO_EXTENSIONS = setOf(
            "mp3", "flac", "ogg", "opus", "m4a", "aac", "wav", "wave",
        )

        private val AUDIO_MIMES = setOf(
            "audio/mpeg", "audio/flac", "audio/ogg", "audio/mp4", "audio/aac",
            "audio/x-wav", "audio/wav", "audio/opus",
        )
    }
}
