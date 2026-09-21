package com.spectra.logger.feature.settings.config

import com.spectra.logger.core.model.*
import com.spectra.logger.core.model.AppContext
import com.spectra.logger.core.utils.*
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.logs.sink.LogSink
import com.spectra.logger.feature.network.sink.NetworkLogSink

/**
 * Configuration for the Spectra Logger.
 * Use [configure] DSL to create instances.
 */
data class LoggerConfiguration(
    val minLogLevel: LogLevel = LogLevel.VERBOSE,
    val logStorageConfig: StorageConfiguration = StorageConfiguration(),
    val networkStorageConfig: StorageConfiguration = StorageConfiguration(maxCapacity = 1_000),
    val eventStorageConfig: StorageConfiguration = StorageConfiguration(maxCapacity = 5_000),
    val crashStorageConfig: StorageConfiguration = StorageConfiguration(maxCapacity = 100),
    val performanceConfig: PerformanceConfiguration = PerformanceConfiguration(),
    val enabledFeatures: FeatureFlags = FeatureFlags(),
    val appContext: AppContext? = null,
    val logSinks: List<LogSink> = emptyList(),
    val networkLogSinks: List<NetworkLogSink> = emptyList(),
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
    val directoryPath: String? = null,
    val flushThreshold: Int = 50,
    val maxFileSizeBytes: Long = 50_000_000L,
    val fileLogLevel: LogLevel = LogLevel.DEBUG,
    val retentionPolicy: com.spectra.logger.core.storage.RetentionPolicy =
        com.spectra.logger.core.storage.RetentionPolicy.DEFAULT,
)

/**
 * Performance-related configuration.
 */
data class PerformanceConfiguration(
    val flowBufferCapacity: Int = 64,
    // milliseconds
    val asyncWriteTimeout: Long = 5_000,
    // bytes for network logs
    val maxBodySize: Int = 10_000,
)

/**
 * Feature flags for enabling/disabling specific features.
 */
data class FeatureFlags(
    val enableNetworkLogging: Boolean = true,
    val enableEventLogging: Boolean = true,
    val enableCrashReporting: Boolean = false,
    val enablePerformanceMetrics: Boolean = false,
    val enableSourceDetection: Boolean = false,
    val networkIgnoredDomains: List<String> = emptyList(),
    val networkIgnoredTokens: List<String> = emptyList(),
    val networkIgnoredExtensions: List<String> = listOf("png", "jpg", "jpeg", "gif", "svg", "ico"),
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
    var appContext: AppContext? = null

    private var logStorageConfig = StorageConfiguration()
    private var networkStorageConfig = StorageConfiguration(maxCapacity = 1_000)
    private var eventStorageConfig = StorageConfiguration(maxCapacity = 5_000)
    private var crashStorageConfig = StorageConfiguration(maxCapacity = 100)
    private var performanceConfig = PerformanceConfiguration()
    private var enabledFeatures = FeatureFlags()
    private val logSinks = mutableListOf<LogSink>()
    private val networkLogSinks = mutableListOf<NetworkLogSink>()

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
     * Configure event log storage settings.
     */
    fun eventStorage(block: StorageConfigurationBuilder.() -> Unit) {
        eventStorageConfig =
            StorageConfigurationBuilder(maxCapacity = 5_000).apply(block).build()
    }

    /**
     * Configure crash storage settings.
     */
    fun crashStorage(block: StorageConfigurationBuilder.() -> Unit) {
        crashStorageConfig =
            StorageConfigurationBuilder(maxCapacity = 100).apply(block).build()
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

    /**
     * Register a custom sink to receive standard logs in real-time.
     */
    fun addSink(sink: LogSink) {
        logSinks.add(sink)
    }

    /**
     * Register a custom sink to receive network logs in real-time.
     */
    fun addNetworkSink(sink: NetworkLogSink) {
        networkLogSinks.add(sink)
    }

    internal fun build(): LoggerConfiguration =
        LoggerConfiguration(
            minLogLevel = minLogLevel,
            logStorageConfig = logStorageConfig,
            networkStorageConfig = networkStorageConfig,
            eventStorageConfig = eventStorageConfig,
            crashStorageConfig = crashStorageConfig,
            performanceConfig = performanceConfig,
            enabledFeatures = enabledFeatures,
            appContext = appContext,
            logSinks = logSinks.toList(),
            networkLogSinks = networkLogSinks.toList(),
        )
}

/**
 * Builder for StorageConfiguration.
 */
@LoggerConfigurationDsl
class StorageConfigurationBuilder(
    var maxCapacity: Int = 10_000,
    var enablePersistence: Boolean = false,
    var directoryPath: String? = null,
    var flushThreshold: Int = 50,
    var maxFileSizeBytes: Long = 50_000_000L,
    var fileLogLevel: LogLevel = LogLevel.DEBUG,
    var retentionPolicy: com.spectra.logger.core.storage.RetentionPolicy =
        com.spectra.logger.core.storage.RetentionPolicy.DEFAULT,
) {
    internal fun build(): StorageConfiguration =
        StorageConfiguration(
            maxCapacity = maxCapacity,
            enablePersistence = enablePersistence,
            directoryPath = directoryPath,
            flushThreshold = flushThreshold,
            maxFileSizeBytes = maxFileSizeBytes,
            fileLogLevel = fileLogLevel,
            retentionPolicy = retentionPolicy,
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
    var enableEventLogging: Boolean = true,
    var enableCrashReporting: Boolean = false,
    var enablePerformanceMetrics: Boolean = false,
    var enableSourceDetection: Boolean = false,
    var networkIgnoredDomains: List<String> = emptyList(),
    var networkIgnoredTokens: List<String> = emptyList(),
    var networkIgnoredExtensions: List<String> = listOf("png", "jpg", "jpeg", "gif", "svg", "ico"),
) {
    internal fun build(): FeatureFlags =
        FeatureFlags(
            enableNetworkLogging = enableNetworkLogging,
            enableEventLogging = enableEventLogging,
            enableCrashReporting = enableCrashReporting,
            enablePerformanceMetrics = enablePerformanceMetrics,
            enableSourceDetection = enableSourceDetection,
            networkIgnoredDomains = networkIgnoredDomains,
            networkIgnoredTokens = networkIgnoredTokens,
            networkIgnoredExtensions = networkIgnoredExtensions,
        )
}

/**
 * DSL function to create logger configuration.
 *
 * Example (minimal):
 * ```
 * val config = configure {
 *     appContext = AppContext(sessionId = UUID.random().toString())
 * }
 * ```
 *
 * Example (full):
 * ```
 * val config = configure {
 *     minLogLevel = LogLevel.DEBUG
 *     appContext = AppContext(
 *         sessionId = UUID.random().toString(),
 *         appVersion = "1.0.0",
 *         buildNumber = "42",
 *         deviceModel = "iPhone14Pro",
 *         osVersion = "17.0",
 *         osName = "iOS",
 *         userId = "user123",
 *         environment = "production"
 *     )
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
fun configure(block: LoggerConfigurationBuilder.() -> Unit): LoggerConfiguration = LoggerConfigurationBuilder().apply(block).build()
