package com.spectra.logger.config

import com.spectra.logger.domain.model.LogLevel

/**
 * Configuration for the Spectra Logger.
 * Use [configure] DSL to create instances.
 */
data class LoggerConfiguration(
    val minLogLevel: LogLevel = LogLevel.VERBOSE,
    val logStorageConfig: StorageConfiguration = StorageConfiguration(),
    val networkStorageConfig: StorageConfiguration = StorageConfiguration(maxCapacity = 1_000),
    val performanceConfig: PerformanceConfiguration = PerformanceConfiguration(),
    val enabledFeatures: FeatureFlags = FeatureFlags(),
) {
    companion object {
        /**
         * Default configuration.
         */
        val DEFAULT = LoggerConfiguration()
    }
}

/**
 * Storage configuration for logs or network logs.
 */
data class StorageConfiguration(
    val maxCapacity: Int = 10_000,
    val enablePersistence: Boolean = false,
)

/**
 * Performance-related configuration.
 */
data class PerformanceConfiguration(
    val flowBufferCapacity: Int = 64,
    val asyncWriteTimeout: Long = 5_000, // milliseconds
    val maxBodySize: Int = 10_000, // bytes for network logs
)

/**
 * Feature flags for enabling/disabling specific features.
 */
data class FeatureFlags(
    val enableNetworkLogging: Boolean = true,
    val enableCrashReporting: Boolean = false,
    val enablePerformanceMetrics: Boolean = false,
)

/**
 * DSL for creating logger configuration.
 */
@DslMarker
annotation class LoggerConfigurationDsl

/**
 * Builder for LoggerConfiguration using Kotlin DSL.
 */
@LoggerConfigurationDsl
class LoggerConfigurationBuilder {
    var minLogLevel: LogLevel = LogLevel.VERBOSE

    private var logStorageConfig = StorageConfiguration()
    private var networkStorageConfig = StorageConfiguration(maxCapacity = 1_000)
    private var performanceConfig = PerformanceConfiguration()
    private var enabledFeatures = FeatureFlags()

    /**
     * Configure log storage settings.
     */
    fun logStorage(block: StorageConfigurationBuilder.() -> Unit) {
        logStorageConfig = StorageConfigurationBuilder().apply(block).build()
    }

    /**
     * Configure network log storage settings.
     */
    fun networkStorage(block: StorageConfigurationBuilder.() -> Unit) {
        networkStorageConfig =
            StorageConfigurationBuilder(maxCapacity = 1_000).apply(block).build()
    }

    /**
     * Configure performance settings.
     */
    fun performance(block: PerformanceConfigurationBuilder.() -> Unit) {
        performanceConfig = PerformanceConfigurationBuilder().apply(block).build()
    }

    /**
     * Configure feature flags.
     */
    fun features(block: FeatureFlagsBuilder.() -> Unit) {
        enabledFeatures = FeatureFlagsBuilder().apply(block).build()
    }

    internal fun build(): LoggerConfiguration =
        LoggerConfiguration(
            minLogLevel = minLogLevel,
            logStorageConfig = logStorageConfig,
            networkStorageConfig = networkStorageConfig,
            performanceConfig = performanceConfig,
            enabledFeatures = enabledFeatures,
        )
}

/**
 * Builder for StorageConfiguration.
 */
@LoggerConfigurationDsl
class StorageConfigurationBuilder(
    var maxCapacity: Int = 10_000,
    var enablePersistence: Boolean = false,
) {
    internal fun build(): StorageConfiguration =
        StorageConfiguration(
            maxCapacity = maxCapacity,
            enablePersistence = enablePersistence,
        )
}

/**
 * Builder for PerformanceConfiguration.
 */
@LoggerConfigurationDsl
class PerformanceConfigurationBuilder(
    var flowBufferCapacity: Int = 64,
    var asyncWriteTimeout: Long = 5_000,
    var maxBodySize: Int = 10_000,
) {
    internal fun build(): PerformanceConfiguration =
        PerformanceConfiguration(
            flowBufferCapacity = flowBufferCapacity,
            asyncWriteTimeout = asyncWriteTimeout,
            maxBodySize = maxBodySize,
        )
}

/**
 * Builder for FeatureFlags.
 */
@LoggerConfigurationDsl
class FeatureFlagsBuilder(
    var enableNetworkLogging: Boolean = true,
    var enableCrashReporting: Boolean = false,
    var enablePerformanceMetrics: Boolean = false,
) {
    internal fun build(): FeatureFlags =
        FeatureFlags(
            enableNetworkLogging = enableNetworkLogging,
            enableCrashReporting = enableCrashReporting,
            enablePerformanceMetrics = enablePerformanceMetrics,
        )
}

/**
 * DSL function to create logger configuration.
 *
 * Example:
 * ```
 * val config = configure {
 *     minLogLevel = LogLevel.DEBUG
 *     logStorage {
 *         maxCapacity = 20_000
 *     }
 *     networkStorage {
 *         maxCapacity = 2_000
 *     }
 *     performance {
 *         flowBufferCapacity = 128
 *     }
 * }
 * ```
 */
fun configure(block: LoggerConfigurationBuilder.() -> Unit): LoggerConfiguration =
    LoggerConfigurationBuilder().apply(block).build()
