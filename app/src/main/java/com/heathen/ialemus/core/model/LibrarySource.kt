package com.heathen.ialemus.core.model

enum class LibrarySourceType {
    SAF_FOLDER,
    MEDIASTORE_FULL_DEVICE,
    FUTURE_NAS,
}

data class LibrarySource(
    val id: String,
    val type: LibrarySourceType,
    val displayName: String,
    val treeUri: String,
    val addedAt: Long,
)
