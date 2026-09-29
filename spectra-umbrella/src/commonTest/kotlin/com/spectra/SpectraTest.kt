package com.spectra

import com.spectra.logger.feature.crash.model.BreadcrumbType
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.logs.model.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpectraTest {
    @Test
    fun testSpectraFacadeAccess() {
        assertNotNull(Spectra.logger)
        assertTrue(Spectra.version.isNotEmpty())
    }

    @Test
    fun testSpectraConfigure() {
        Spectra.configure {
            minLogLevel = LogLevel.DEBUG
            logStorage {
                maxCapacity = 500
            }
        }
        assertEquals(LogLevel.DEBUG, Spectra.logger.configuration.minLogLevel)
    }

    @Test
    fun testSpectraLoggingMethods() {
        Spectra.v("TAG", "Verbose test log")
        Spectra.d("TAG", "Debug test log")
        Spectra.i("TAG", "Info test log")
        Spectra.w("TAG", "Warn test log")
        Spectra.e("TAG", "Error test log", RuntimeException("Simulated error"))
        Spectra.f("TAG", "Fatal test log")
    }

    @Test
    fun testSpectraTelemetryMethods() {
        Spectra.event(
            name = "umbrella_test_event",
            parameters = mapOf("source" to "SpectraTest"),
            eventType = EventType.CUSTOM,
            durationMs = 120L,
        )

        Spectra.screenStart("HomeScreen", mapOf("tab" to "feed"))
        Spectra.screenEnd("HomeScreen", mapOf("success" to "true"))

        Spectra.recordBreadcrumb(
            type = BreadcrumbType.USER_ACTION,
            category = "navigation",
            message = "User navigated to HomeScreen",
        )

        Spectra.recordNonFatalException(
            throwable = IllegalStateException("Non-fatal test issue"),
            metadata = mapOf("context" to "unit_test"),
        )
    }
}
