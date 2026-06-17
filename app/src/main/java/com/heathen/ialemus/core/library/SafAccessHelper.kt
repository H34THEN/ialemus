package com.heathen.ialemus.core.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.heathen.ialemus.core.model.LibrarySource

class SafAccessHelper(
    private val context: Context,
) {
    fun restorePersistedFolderPermissions(sources: List<LibrarySource>) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sources.forEach { source ->
            val uri = runCatching { Uri.parse(source.treeUri) }.getOrNull() ?: return@forEach
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, flags)
            }
        }
    }

    fun canReadSource(source: LibrarySource): Boolean {
        val uri = runCatching { Uri.parse(source.treeUri) }.getOrNull() ?: return false
        val root = DocumentFile.fromTreeUri(context, uri) ?: return false
        return root.canRead() && root.exists()
    }

    fun isFolderConfirmedEmpty(source: LibrarySource): Boolean {
        if (!canReadSource(source)) return false
        val uri = Uri.parse(source.treeUri)
        val root = DocumentFile.fromTreeUri(context, uri) ?: return false
        return !folderContainsAudio(root)
    }

    private fun folderContainsAudio(folder: DocumentFile): Boolean {
        for (child in folder.listFiles()) {
            if (child.isDirectory && folderContainsAudio(child)) return true
            if (child.isFile && isLikelyAudio(child)) return true
        }
        return false
    }

    private fun isLikelyAudio(file: DocumentFile): Boolean {
        val mime = file.type?.lowercase()
        if (mime != null && mime.startsWith("audio/")) return true
        val ext = file.name?.substringAfterLast('.', "")?.lowercase().orEmpty()
        return ext in AUDIO_EXTENSIONS
    }

    companion object {
        private val AUDIO_EXTENSIONS = setOf(
            "mp3", "flac", "ogg", "opus", "m4a", "aac", "wav", "wave",
        )
    }
}
