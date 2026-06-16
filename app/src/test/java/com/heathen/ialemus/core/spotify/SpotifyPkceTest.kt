package com.heathen.ialemus.core.spotify

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.regex.Pattern

class SpotifyPkceTest {
    @Test
    fun codeVerifier_hasExpectedLengthAndCharset() {
        val verifier = SpotifyPkce.generateCodeVerifier()
        assertEquals(64, verifier.length)
        assertTrue(PKCE_PATTERN.matcher(verifier).matches())
    }

    @Test
    fun codeChallenge_isDeterministicForVerifier() {
        val verifier = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
        val challenge1 = SpotifyPkce.generateCodeChallenge(verifier)
        val challenge2 = SpotifyPkce.generateCodeChallenge(verifier)
        assertEquals(challenge1, challenge2)
        assertTrue(challenge1.isNotBlank())
    }

    @Test
    fun createSession_generatesUniqueStateAndVerifier() {
        val session1 = SpotifyPkce.createSession()
        val session2 = SpotifyPkce.createSession()
        assertNotEquals(session1.state, session2.state)
        assertNotEquals(session1.codeVerifier, session2.codeVerifier)
        assertTrue(session1.codeChallenge.isNotBlank())
    }

    companion object {
        private val PKCE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-._~]+$")
    }
}
