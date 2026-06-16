package com.heathen.ialemus.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ServiceUrlTesterTest {
    @Test
    fun statusForConfiguredUrl_blank_isNotConfigured() {
        assertEquals(
            ConnectionTestStatus.NOT_CONFIGURED,
            ServiceUrlTester.statusForConfiguredUrl("", null, false),
        )
    }

    @Test
    fun statusForConfiguredUrl_configuredNoTest_isReady() {
        assertEquals(
            ConnectionTestStatus.READY,
            ServiceUrlTester.statusForConfiguredUrl("http://192.168.1.1:8080", null, false),
        )
    }

    @Test
    fun statusForConfiguredUrl_checking_returnsChecking() {
        assertEquals(
            ConnectionTestStatus.CHECKING,
            ServiceUrlTester.statusForConfiguredUrl("http://192.168.1.1:8080", null, true),
        )
    }

    @Test
    fun statusForConfiguredUrl_success_isReachable() {
        assertEquals(
            ConnectionTestStatus.REACHABLE,
            ServiceUrlTester.statusForConfiguredUrl("http://192.168.1.1:8080", Result.success(200), false),
        )
    }

    @Test
    fun statusForConfiguredUrl_failure_isFailed() {
        assertEquals(
            ConnectionTestStatus.FAILED,
            ServiceUrlTester.statusForConfiguredUrl(
                "http://192.168.1.1:8080",
                Result.failure(Exception("timeout")),
                false,
            ),
        )
    }
}
