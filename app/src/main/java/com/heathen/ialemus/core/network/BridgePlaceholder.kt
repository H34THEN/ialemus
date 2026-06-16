package com.heathen.ialemus.core.network

/**
 * Placeholder for Ialemus Bridge HTTP client.
 *
 * The Android app must never execute raw shell, SSH, or Docker commands.
 * All acquisition flows go through authenticated bridge API endpoints.
 *
 * TODO(MVP 2): Add Retrofit or Ktor client for health, jobs, and library endpoints.
 */
object BridgePlaceholder {
    const val ARCHITECTURE_NOTE =
        "Acquisition requests use Ialemus Bridge API — not direct shell commands."
}
