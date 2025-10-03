package com.spectra.logger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Basic test to verify the project setup works.
 */
class SpectraLoggerTest {
    @Test
    fun testGetVersion() {
        val version = SpectraLogger.getVersion()
        assertTrue(version.isNotEmpty(), "Version should not be empty")
        assertEquals("0.0.1-SNAPSHOT", version)
    }
}
