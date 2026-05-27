package com.spectra.logger

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

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
        assertEquals(Version.LIBRARY_VERSION, version)
    }
}
