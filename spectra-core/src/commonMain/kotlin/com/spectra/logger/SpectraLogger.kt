package com.spectra.logger

import com.spectra.logger.core.Logger
import com.spectra.logger.core.model.*
import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.core.utils.*
import com.spectra.logger.core.utils.ioDispatcher
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogFilter
import com.spectra.logger.feature.logs.storage.FileLogStorage
import com.spectra.logger.feature.logs.storage.InMemoryLogStorage
import com.spectra.logger.feature.logs.storage.LogStorage
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.feature.network.storage.InMemoryNetworkLogStorage
import com.spectra.logger.feature.network.storage.NetworkLogStorage
import com.spectra.logger.feature.settings.config.LoggerConfiguration
import com.spectra.logger.feature.settings.config.LoggerConfigurationBuilder
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
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
    private val configAtomic = atomic<LoggerConfiguration?>(null)
    private val logStorageAtomic =
        atomic<LogStorage?>(null)
    private val networkStorageAtomic =
        atomic<NetworkLogStorage?>(null)
    private val exceptionHandler =
        kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            // Silently swallow internal storage exceptions to prevent app crashes
            // but log them to the local console for debugging
            println("SpectraLogger Internal Error: ${throwable.message}")
        }

    private var ioScope = CoroutineScope(SupervisorJob() + ioDispatcher + exceptionHandler)
        set(value) {
            field = value
            // Re-bind the active logger when scope changes
            loggerAtomic.value =
                Logger(
                    storage = logStorage,
                    sinks = configuration.logSinks,
                    minLevel = configuration.minLogLevel,
                    scope = value,
                )
        }

    private val loggerAtomic =
        atomic<Logger?>(null)

    /**
     * Current configuration.
     */
    public val configuration: LoggerConfiguration
        get() = configAtomic.value ?: LoggerConfiguration.DEFAULT

    /**
     * Log storage instance.
     */
    public val logStorage: LogStorage
        get() {
            var current = logStorageAtomic.value
            if (current == null) {
                current = InMemoryLogStorage(maxCapacity = configuration.logStorageConfig.maxCapacity)
                logStorageAtomic.compareAndSet(null, current)
            }
            return logStorageAtomic.value!!
        }

    /**
     * Network log storage instance.
     */
    public val networkStorage: NetworkLogStorage
        get() {
            var current = networkStorageAtomic.value
            if (current == null) {
                current = InMemoryNetworkLogStorage(maxCapacity = configuration.networkStorageConfig.maxCapacity)
                networkStorageAtomic.compareAndSet(null, current)
            }
            return networkStorageAtomic.value!!
        }

    private val logger: Logger
        get() {
            var current = loggerAtomic.value
            if (current == null) {
                current =
                    Logger(
                        storage = logStorage,
                        sinks = configuration.logSinks,
                        minLevel = configuration.minLogLevel,
                        scope = ioScope,
                    )
                loggerAtomic.compareAndSet(null, current)
            }
            return loggerAtomic.value!!
        }

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
        ioScope = CoroutineScope(SupervisorJob() + ioDispatcher + exceptionHandler)
    }

    /**
     * Log a network event without blocking.
     */
    fun logNetwork(entry: NetworkLogEntry) {
        if (!configuration.enabledFeatures.enableNetworkLogging) return
        ioScope.launch {
            // Run local storage concurrently with sinks so it doesn't block plugin execution
            networkStorage.add(entry)

            // Fan-out to custom network sinks sequentially within this coroutine to prevent launch explosion
            configuration.networkLogSinks.forEach { sink ->
                runCatching {
                    sink.logNetwork(entry)
                } // Silently swallow to prevent stdout pollution and app crashes
            }
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

        val currentLogStorage = logStorage
        val newStorage =
            if (newConfig.logStorageConfig.enablePersistence && newConfig.logStorageConfig.directoryPath != null) {
                val fileSystem = FileSystem(newConfig.logStorageConfig.directoryPath!!)
                if (fileSystem.okioFs != null) {
                    if (currentLogStorage is FileLogStorage) {
                        ioScope.launch { currentLogStorage.close() }
                    }
                    FileLogStorage(
                        fileSystem = fileSystem,
                        maxFileSize = newConfig.logStorageConfig.maxFileSizeBytes ?: FileLogStorage.DEFAULT_MAX_FILE_SIZE,
                        flushThreshold = newConfig.logStorageConfig.flushThreshold ?: 50,
                        maxCapacity = newConfig.logStorageConfig.maxCapacity,
                    )
                } else {
                    if (currentLogStorage is FileLogStorage) {
                        ioScope.launch { currentLogStorage.close() }
                    }
                    InMemoryLogStorage(maxCapacity = newConfig.logStorageConfig.maxCapacity)
                }
            } else if (currentLogStorage is InMemoryLogStorage) {
                currentLogStorage.updateCapacity(newConfig.logStorageConfig.maxCapacity)
                currentLogStorage
            } else {
                if (currentLogStorage is FileLogStorage) {
                    ioScope.launch { currentLogStorage.close() }
                }
                InMemoryLogStorage(maxCapacity = newConfig.logStorageConfig.maxCapacity)
            }
        logStorageAtomic.value = newStorage

        val currentNetworkStorage = networkStorage
        if (currentNetworkStorage is InMemoryNetworkLogStorage) {
            currentNetworkStorage.updateCapacity(newConfig.networkStorageConfig.maxCapacity)
        } else {
            networkStorageAtomic.value = InMemoryNetworkLogStorage(maxCapacity = newConfig.networkStorageConfig.maxCapacity)
        }

        loggerAtomic.value =
            Logger(
                storage = logStorageAtomic.value!!,
                sinks = newConfig.logSinks,
                minLevel = newConfig.minLogLevel,
                scope = ioScope,
            )
    }
}
