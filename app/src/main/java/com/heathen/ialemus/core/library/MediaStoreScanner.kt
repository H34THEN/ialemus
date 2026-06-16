package com.heathen.ialemus.core.library

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.heathen.ialemus.core.model.SourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.model.TrackOrigin
import com.heathen.ialemus.core.model.localTrackId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(
    private val context: Context,
    private val minimumDurationMs: Long = 30_000L,
) {
    suspend fun scan(): List<Track> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val projection = buildProjection()
        val selection = """
            ${MediaStore.Audio.Media.IS_MUSIC} != 0
            AND ${MediaStore.Audio.Media.DURATION} >= ?
        """.trimIndent()
        val selectionArgs = arrayOf(minimumDurationMs.toString())

        val tracks = mutableListOf<Track>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val relativePathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val mediaStoreId = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaStoreId,
                ).toString()
                val trackNumberRaw = cursor.getInt(trackCol)
                val trackNumber = if (trackNumberRaw > 0) trackNumberRaw else null

                tracks += Track(
                    id = localTrackId(mediaStoreId),
                    mediaStoreId = mediaStoreId,
                    title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Unknown Title",
                    artist = cursor.getString(artistCol),
                    album = cursor.getString(albumCol),
                    albumId = cursor.getLong(albumIdCol),
                    durationMs = cursor.getLong(durationCol),
                    trackNumber = trackNumber,
                    discNumber = null,
                    contentUri = contentUri,
                    relativePath = if (relativePathCol >= 0) cursor.getString(relativePathCol) else null,
                    dateAdded = cursor.getLong(dateAddedCol),
                    dateModified = cursor.getLong(dateModifiedCol),
                    size = cursor.getLong(sizeCol),
                    sourceType = SourceType.LOCAL,
                    origin = TrackOrigin.MANUAL,
                    lastScannedAt = now,
                )
            }
        }

        // TODO(MVP 1B): Incremental scan using MediaStore generation markers on API 30+.
        tracks
    }

    private fun buildProjection(): Array<String> {
        val base = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.IS_MUSIC,
        )
        return base.toTypedArray()
    }
}
