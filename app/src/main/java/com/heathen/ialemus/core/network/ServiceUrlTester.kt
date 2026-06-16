package com.heathen.ialemus.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

object ServiceUrlTester {
    private const val CONNECT_TIMEOUT_MS = 5_000
    private const val READ_TIMEOUT_MS = 5_000

    suspend fun testGet(
        url: String,
        bearerToken: String? = null,
    ): Result<Int> = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("URL not configured"))
        }
        runCatching {
            val parsed = URI(trimmed)
            if (parsed.scheme.isNullOrBlank()) {
                throw IllegalArgumentException("URL must include http:// or https://")
            }
            val connection = URL(parsed.toString()).openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true
            if (!bearerToken.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            try {
                connection.responseCode
            } finally {
                connection.disconnect()
            }
        }
    }

    fun statusForConfiguredUrl(
        url: String,
        lastResult: Result<Int>?,
        isChecking: Boolean,
    ): ConnectionTestStatus {
        if (url.isBlank()) return ConnectionTestStatus.NOT_CONFIGURED
        if (isChecking) return ConnectionTestStatus.CHECKING
        return when (val result = lastResult) {
            null -> ConnectionTestStatus.READY
            else -> if (result.isSuccess) ConnectionTestStatus.REACHABLE else ConnectionTestStatus.FAILED
        }
    }

    fun isReachable(result: Result<Int>?): Boolean {
        val code = result?.getOrNull() ?: return false
        return code in 200..399
    }
}
