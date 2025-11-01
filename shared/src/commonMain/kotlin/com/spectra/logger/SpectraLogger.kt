package com.spectra.logger

import com.spectra.logger.config.LoggerConfiguration
import com.spectra.logger.config.LoggerConfigurationBuilder
import com.spectra.logger.domain.Logger
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.model.NetworkLogFilter
import com.spectra.logger.domain.storage.InMemoryLogStorage
import com.spectra.logger.domain.storage.InMemoryNetworkLogStorage
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.domain.storage.NetworkLogStorage
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for the Spectra Logger framework.
 * Provides a simple, global API for logging.
 *
 * Initialize with custom configuration:
 * ```
 * SpectraLogger.configure {
 *     minLogLevel = LogLevel.DEBUG
 *     logStorage {
 *         maxCapacity = 20_000
 *     }
 * }
 * ```
 *
 * @since 0.0.1
 */
object SpectraLogger {
    private val configAtomic = atomic(LoggerConfiguration.DEFAULT)
    private val logStorageAtomic =
        atomic<LogStorage>(InMemoryLogStorage(maxCapacity = LoggerConfiguration.DEFAULT.logStorageConfig.maxCapacity))
    private val networkStorageAtomic =
        atomic<NetworkLogStorage>(
            InMemoryNetworkLogStorage(maxCapacity = LoggerConfiguration.DEFAULT.networkStorageConfig.maxCapacity),
        )
    private val loggerAtomic =
        atomic(
            Logger(
                storage = logStorageAtomic.value,
                minLevel = LoggerConfiguration.DEFAULT.minLogLevel,
            ),
        )

    /**
     * Current configuration.
     */
    val configuration: LoggerConfiguration
        get() = configAtomic.value

    /**
     * Log storage instance.
     */
    val logStorage: LogStorage
        get() = logStorageAtomic.value

    /**
     * Network log storage instance.
     */
    val networkStorage: NetworkLogStorage
        get() = networkStorageAtomic.value

    private val logger: Logger
        get() = loggerAtomic.value

    /**
     * Returns the current version of the Spectra Logger framework.
     * Version is automatically synchronized from gradle.properties during build.
     */
    fun getVersion(): String = Version.LIBRARY_VERSION

    /**
     * Log verbose message.
     */
    fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) = logger.v(tag, message, throwable, metadata)

    /**
     * Log debug message.
     */
    fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) = logger.d(tag, message, throwable, metadata)

    /**
     * Log info message.
     */
    fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) = logger.i(tag, message, throwable, metadata)

    /**
     * Log warning message.
     */
    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) = logger.w(tag, message, throwable, metadata)

    /**
     * Log error message.
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) = logger.e(tag, message, throwable, metadata)

    /**
     * Log fatal error message.
     */
    fun f(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) = logger.f(tag, message, throwable, metadata)

    /**
     * Query stored logs.
     */
    suspend fun query(
        filter: LogFilter = LogFilter.NONE,
        limit: Int? = null,
    ): List<LogEntry> = logger.query(filter, limit)

    /**
     * Observe logs as a flow.
     */
    fun observe(filter: LogFilter = LogFilter.NONE): Flow<LogEntry> = logger.observe(filter)

    /**
     * Get total log count.
     */
    suspend fun count(): Int = logger.count()

    /**
     * Clear all logs.
     */
    suspend fun clear() = logger.clear()

    // Network Logging API

    /**
     * Query stored network logs.
     */
    suspend fun queryNetwork(
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
        limit: Int? = null,
    ): List<NetworkLogEntry> = networkStorage.query(filter, limit)

    /**
     * Observe network logs as a flow.
     */
    fun observeNetwork(filter: NetworkLogFilter = NetworkLogFilter.NONE): Flow<NetworkLogEntry> =
        networkStorage.observe(filter)

    /**
     * Get total network log count.
     */
    suspend fun networkCount(): Int = networkStorage.count()

    /**
     * Clear all network logs.
     */
    suspend fun clearNetwork() = networkStorage.clear()

    // Configuration API

    /**
     * Configure the logger with custom settings.
     * Must be called before any logging occurs for settings to take effect.
     *
     * Example:
     * ```
     * SpectraLogger.configure {
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
    fun configure(block: LoggerConfigurationBuilder.() -> Unit) {
        val newConfig = LoggerConfigurationBuilder().apply(block).build()
        configAtomic.value = newConfig

        // Recreate storages with new configuration
        val newLogStorage = InMemoryLogStorage(maxCapacity = newConfig.logStorageConfig.maxCapacity)
        val newNetworkStorage = InMemoryNetworkLogStorage(maxCapacity = newConfig.networkStorageConfig.maxCapacity)

        logStorageAtomic.value = newLogStorage
        networkStorageAtomic.value = newNetworkStorage

        // Recreate logger with new configuration
        loggerAtomic.value =
            Logger(
                storage = newLogStorage,
                minLevel = newConfig.minLogLevel,
            )
    }
}
