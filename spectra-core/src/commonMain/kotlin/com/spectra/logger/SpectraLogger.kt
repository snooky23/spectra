package com.spectra.logger

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import com.spectra.logger.feature.settings.config.LoggerConfiguration
import com.spectra.logger.feature.settings.config.LoggerConfigurationBuilder
import com.spectra.logger.core.Logger
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogFilter
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.logs.storage.InMemoryLogStorage
import com.spectra.logger.feature.logs.storage.FileLogStorage
import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.feature.network.storage.InMemoryNetworkLogStorage
import com.spectra.logger.feature.logs.storage.LogStorage
import com.spectra.logger.feature.network.storage.NetworkLogStorage
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main entry point for the Spectra Logger framework.
 * Provides a simple, global API for logging, querying, and accessing the debug UI.
 *
 * **Logging:**
 * ```
 * SpectraLogger.d("TAG", "Debug message")
 * SpectraLogger.e("TAG", "Error", throwable = exception)
 * ```
 *
 * **Configuration:**
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
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (industry standard: nullable)
     */
    fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) = logger.v(tag, message, throwable, metadata)

    /**
     * Log debug message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (industry standard: nullable)
     */
    fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) = logger.d(tag, message, throwable, metadata)

    /**
     * Log info message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (industry standard: nullable)
     */
    fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) = logger.i(tag, message, throwable, metadata)

    /**
     * Log warning message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (industry standard: nullable)
     */
    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) = logger.w(tag, message, throwable, metadata)

    /**
     * Log error message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (industry standard: nullable)
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) = logger.e(tag, message, throwable, metadata)

    /**
     * Log fatal error message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (industry standard: nullable)
     */
    fun f(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) = logger.f(tag, message, throwable, metadata)

    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        // Silently swallow network storage exceptions to prevent app crashes
        // but log them to the local console for debugging
        println("SpectraLogger Network Storage Error: ${throwable.message}")
    }

    private var ioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)
    
    /**
     * Internal setter for testing
     */
    internal fun setCoroutineScopeForTesting(scope: CoroutineScope) {
        ioScope = scope
    }

    /**
     * Internal reset for testing
     */
    internal fun resetCoroutineScopeForTesting() {
        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)
    }
    
    /**
     * Log a network event without blocking.
     */
    fun logNetwork(entry: NetworkLogEntry) {
        ioScope.launch {
            networkStorage.add(entry)
        }
    }

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

    /**
     * Export all log entries to a single file and return its absolute path.
     * @return Absolute path to the exported `.jsonl` file, or null if empty/failed.
     */
    suspend fun exportLogs(): String? = logger.exportLogs()

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
    fun observeNetwork(filter: NetworkLogFilter = NetworkLogFilter.NONE): Flow<NetworkLogEntry> = networkStorage.observe(filter)

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

        val currentLogStorage = logStorageAtomic.value
        if (newConfig.logStorageConfig.enablePersistence && newConfig.logStorageConfig.directoryPath != null) {
            val fileSystem = FileSystem(newConfig.logStorageConfig.directoryPath!!)
            logStorageAtomic.value = FileLogStorage(
                fileSystem = fileSystem,
                maxFileSize = newConfig.logStorageConfig.maxFileSizeBytes ?: FileLogStorage.DEFAULT_MAX_FILE_SIZE,
                flushThreshold = newConfig.logStorageConfig.flushThreshold ?: 50,
                maxCapacity = newConfig.logStorageConfig.maxCapacity
            )
        } else if (currentLogStorage is InMemoryLogStorage) {
            currentLogStorage.updateCapacity(newConfig.logStorageConfig.maxCapacity)
        } else {
            logStorageAtomic.value = InMemoryLogStorage(maxCapacity = newConfig.logStorageConfig.maxCapacity)
        }

        val currentNetworkStorage = networkStorageAtomic.value
        if (currentNetworkStorage is InMemoryNetworkLogStorage) {
            currentNetworkStorage.updateCapacity(newConfig.networkStorageConfig.maxCapacity)
        } else {
            networkStorageAtomic.value = InMemoryNetworkLogStorage(maxCapacity = newConfig.networkStorageConfig.maxCapacity)
        }

        // Recreate logger with new configuration using the active logStorage
        loggerAtomic.value =
            Logger(
                storage = logStorageAtomic.value,
                minLevel = newConfig.minLogLevel,
            )
    }

}
