package com.heathen.ialemus.core.model

data class RemoteJob(
    val id: String,
    val service: RemoteService,
    val status: JobStatus,
    val input: String,
    val profile: String,
    val outputTarget: String,
    val progressPercent: Double? = null,
    val createdAt: Long,
    val completedAt: Long? = null,
    val errorMessage: String? = null,
)

enum class RemoteService {
    SPOTDL,
    METUBE,
    SLSKD,
    GENERIC_PROFILE,
}

enum class JobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
}
