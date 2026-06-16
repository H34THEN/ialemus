package com.heathen.ialemus.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceUrlValidatorTest {
    @Test
    fun validate_acceptsHttpUrl() {
        val result = ServiceUrlValidator.validate("http://192.168.1.213:38245/")
        assertTrue(result.isSuccess)
        assertEquals("http://192.168.1.213:38245/", result.getOrNull())
    }

    @Test
    fun validate_rejectsBlank() {
        assertTrue(ServiceUrlValidator.validate("  ").isFailure)
    }

    @Test
    fun validate_rejectsNonHttpScheme() {
        assertTrue(ServiceUrlValidator.validate("ftp://192.168.1.1").isFailure)
    }

    @Test
    fun normalize_addsTrailingSlash() {
        assertEquals("http://192.168.1.213:5031/", ServiceUrlValidator.normalize("http://192.168.1.213:5031"))
    }

    @Test
    fun normalizeForLoad_prependsHttpWhenMissing() {
        assertEquals(
            "http://192.168.1.213:38245/",
            ServiceUrlValidator.normalizeForLoad("192.168.1.213:38245"),
        )
    }
}
