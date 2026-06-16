package com.heathen.ialemus.core.network

import java.net.URI

object ServiceUrlValidator {
    private val ALLOWED_SCHEMES = setOf("http", "https")

    fun validate(url: String): Result<String> {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            return Result.failure(ValidationException("URL cannot be blank."))
        }
        val uri = try {
            URI(trimmed)
        } catch (error: Exception) {
            return Result.failure(ValidationException("Invalid URL format."))
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme == null || scheme !in ALLOWED_SCHEMES) {
            return Result.failure(ValidationException("URL must start with http:// or https://"))
        }
        if (uri.host.isNullOrBlank()) {
            return Result.failure(ValidationException("URL must include a host."))
        }
        return Result.success(normalize(trimmed))
    }

    fun normalize(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return trimmed
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    class ValidationException(message: String) : Exception(message)
}
