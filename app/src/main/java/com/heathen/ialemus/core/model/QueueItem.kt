package com.heathen.ialemus.core.model

data class QueueItem(
    val track: Track,
    val queueIndex: Int,
    val isCurrent: Boolean = false,
)
