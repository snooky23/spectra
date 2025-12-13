package com.spectra.logger.config

import com.spectra.logger.domain.model.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoggerConfigurationTest {
    @Test
    fun testDefaultConfiguration() {
        val config = LoggerConfiguration.DEFAULT

        assertEquals(LogLevel.VERBOSE, config.minLogLevel)
        assertEquals(10_000, config.logStorageConfig.maxCapacity)
        assertEquals(1_000, config.networkStorageConfig.maxCapacity)
        assertEquals(64, config.performanceConfig.flowBufferCapacity)
        assertEquals(5_000, config.performanceConfig.asyncWriteTimeout)
        assertEquals(10_000, config.performanceConfig.maxBodySize)
        assertTrue(config.enabledFeatures.enableNetworkLogging)
        assertFalse(config.enabledFeatures.enableCrashReporting)
        assertFalse(config.enabledFeatures.enablePerformanceMetrics)
    }

    @Test
    fun testConfigureMinLogLevel() {
        val config =
            configure {
                minLogLevel = LogLevel.WARNING
            }

        assertEquals(LogLevel.WARNING, config.minLogLevel)
    }

    @Test
    fun testConfigureLogStorage() {
        val config =
            configure {
                logStorage {
                    maxCapacity = 20_000
                    enablePersistence = true
                }
            }

        assertEquals(20_000, config.logStorageConfig.maxCapacity)
        assertTrue(config.logStorageConfig.enablePersistence)
    }

    @Test
    fun testConfigureNetworkStorage() {
        val config =
            configure {
                networkStorage {
                    maxCapacity = 5_000
                    enablePersistence = false
                }
            }

        assertEquals(5_000, config.networkStorageConfig.maxCapacity)
        assertFalse(config.networkStorageConfig.enablePersistence)
    }

    @Test
    fun testConfigurePerformance() {
        val config =
            configure {
                performance {
                    flowBufferCapacity = 128
                    asyncWriteTimeout = 10_000
                    maxBodySize = 20_000
                }
            }

        assertEquals(128, config.performanceConfig.flowBufferCapacity)
        assertEquals(10_000, config.performanceConfig.asyncWriteTimeout)
        assertEquals(20_000, config.performanceConfig.maxBodySize)
    }

    @Test
    fun testConfigureFeatureFlags() {
        val config =
            configure {
                features {
                    enableNetworkLogging = false
                    enableCrashReporting = true
                    enablePerformanceMetrics = true
                }
            }

        assertFalse(config.enabledFeatures.enableNetworkLogging)
        assertTrue(config.enabledFeatures.enableCrashReporting)
        assertTrue(config.enabledFeatures.enablePerformanceMetrics)
    }

    @Test
    fun testCompleteConfiguration() {
        val config =
            configure {
                minLogLevel = LogLevel.DEBUG

                logStorage {
                    maxCapacity = 50_000
                    enablePersistence = true
                }

                networkStorage {
                    maxCapacity = 10_000
                }

                performance {
                    flowBufferCapacity = 256
                    asyncWriteTimeout = 3_000
                    maxBodySize = 50_000
                }

                features {
                    enableNetworkLogging = true
                    enableCrashReporting = false
                    enablePerformanceMetrics = true
                }
            }

        assertEquals(LogLevel.DEBUG, config.minLogLevel)
        assertEquals(50_000, config.logStorageConfig.maxCapacity)
        assertTrue(config.logStorageConfig.enablePersistence)
        assertEquals(10_000, config.networkStorageConfig.maxCapacity)
        assertEquals(256, config.performanceConfig.flowBufferCapacity)
        assertEquals(3_000, config.performanceConfig.asyncWriteTimeout)
        assertEquals(50_000, config.performanceConfig.maxBodySize)
        assertTrue(config.enabledFeatures.enableNetworkLogging)
        assertFalse(config.enabledFeatures.enableCrashReporting)
        assertTrue(config.enabledFeatures.enablePerformanceMetrics)
    }

    @Test
    fun testPartialConfiguration() {
        val config =
            configure {
                minLogLevel = LogLevel.ERROR
                logStorage {
                    maxCapacity = 15_000
                }
                // Other settings should use defaults
            }

        assertEquals(LogLevel.ERROR, config.minLogLevel)
        assertEquals(15_000, config.logStorageConfig.maxCapacity)
        assertEquals(1_000, config.networkStorageConfig.maxCapacity) // default
        assertEquals(64, config.performanceConfig.flowBufferCapacity) // default
    }

    @Test
    fun testEmptyConfiguration() {
        val config = configure { }

        // Should be same as default
        assertEquals(LoggerConfiguration.DEFAULT.minLogLevel, config.minLogLevel)
        assertEquals(LoggerConfiguration.DEFAULT.logStorageConfig, config.logStorageConfig)
        assertEquals(LoggerConfiguration.DEFAULT.networkStorageConfig, config.networkStorageConfig)
        assertEquals(LoggerConfiguration.DEFAULT.performanceConfig, config.performanceConfig)
        assertEquals(LoggerConfiguration.DEFAULT.enabledFeatures, config.enabledFeatures)
    }
}
