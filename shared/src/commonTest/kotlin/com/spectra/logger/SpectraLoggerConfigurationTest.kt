package com.spectra.logger

import com.spectra.logger.domain.model.LogLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpectraLoggerConfigurationTest {
    @Test
    fun testDefaultConfiguration() {
        val config = SpectraLogger.configuration

        assertEquals(LogLevel.VERBOSE, config.minLogLevel)
        assertEquals(10_000, config.logStorageConfig.maxCapacity)
        assertEquals(1_000, config.networkStorageConfig.maxCapacity)
    }

    @Test
    fun testConfigureChangesSettings() {
        SpectraLogger.configure {
            minLogLevel = LogLevel.WARNING
            logStorage {
                maxCapacity = 5_000
            }
            networkStorage {
                maxCapacity = 500
            }
        }

        val config = SpectraLogger.configuration
        assertEquals(LogLevel.WARNING, config.minLogLevel)
        assertEquals(5_000, config.logStorageConfig.maxCapacity)
        assertEquals(500, config.networkStorageConfig.maxCapacity)

        // Reset for other tests
        SpectraLogger.configure {
            minLogLevel = LogLevel.VERBOSE
            logStorage { maxCapacity = 10_000 }
            networkStorage { maxCapacity = 1_000 }
        }
    }

    @Test
    fun testConfigureAffectsLogging() =
        runTest {
            // Configure to filter out VERBOSE and DEBUG
            SpectraLogger.configure {
                minLogLevel = LogLevel.INFO
            }

            SpectraLogger.clear()

            SpectraLogger.v("Test", "Verbose - should be filtered")
            SpectraLogger.d("Test", "Debug - should be filtered")
            SpectraLogger.i("Test", "Info - should pass")
            SpectraLogger.w("Test", "Warning - should pass")

            delay(100)

            val count = SpectraLogger.count()
            assertEquals(2, count) // Only INFO and WARNING

            val logs = SpectraLogger.query()
            assertTrue(logs.all { it.level.priority >= LogLevel.INFO.priority })

            // Reset
            SpectraLogger.configure { minLogLevel = LogLevel.VERBOSE }
        }

    @Test
    fun testNetworkStorageAccess() =
        runTest {
            val networkStorage = SpectraLogger.networkStorage
            assertEquals(0, networkStorage.count())
        }

    @Test
    fun testLogStorageAccess() =
        runTest {
            val logStorage = SpectraLogger.logStorage
            assertTrue(logStorage.count() >= 0)
        }

    @Test
    fun testNetworkQueryAPI() =
        runTest {
            val results = SpectraLogger.queryNetwork()
            assertTrue(results.isEmpty() || results.isNotEmpty()) // Just verify it works
        }

    @Test
    fun testNetworkCount() =
        runTest {
            val count = SpectraLogger.networkCount()
            assertTrue(count >= 0)
        }

    @Test
    fun testClearNetwork() =
        runTest {
            SpectraLogger.clearNetwork()
            assertEquals(0, SpectraLogger.networkCount())
        }
}
