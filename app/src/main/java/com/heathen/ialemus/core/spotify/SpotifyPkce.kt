package com.heathen.ialemus.core.spotify

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SpotifyPkce {
    private const val VERIFIER_LENGTH = 64
    private const val CHARSET =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"

    fun generateCodeVerifier(): String {
        val random = SecureRandom()
        return buildString(VERIFIER_LENGTH) {
            repeat(VERIFIER_LENGTH) {
                append(CHARSET[random.nextInt(CHARSET.length)])
            }
        }
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    fun generateState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun createSession(): PkceSession {
        val verifier = generateCodeVerifier()
        return PkceSession(
            codeVerifier = verifier,
            codeChallenge = generateCodeChallenge(verifier),
            state = generateState(),
        )
    }
}

data class PkceSession(
    val codeVerifier: String,
    val codeChallenge: String,
    val state: String,
)
