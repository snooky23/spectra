package com.spectra.logger

import com.spectra.logger.core.Logger
import com.spectra.logger.core.model.*
import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.core.storage.RetentionPolicy
import com.spectra.logger.core.utils.*
import com.spectra.logger.core.utils.ioDispatcher
import com.spectra.logger.feature.crash.SpectraCrashReporter
import com.spectra.logger.feature.crash.interceptor.BreadcrumbRecorder
import com.spectra.logger.feature.crash.interceptor.DefaultBreadcrumbRecorder
import com.spectra.logger.feature.crash.model.Breadcrumb
import com.spectra.logger.feature.crash.model.BreadcrumbType
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.storage.CrashStorage
import com.spectra.logger.feature.crash.storage.FileCrashStorage
import com.spectra.logger.feature.crash.storage.InMemoryCrashStorage
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.events.storage.EventLogStorage
import com.spectra.logger.feature.events.storage.InMemoryEventLogStorage
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
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

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
    private val eventStorageAtomic =
        atomic<EventLogStorage?>(null)
    private val crashStorageAtomic =
        atomic<CrashStorage?>(null)
    private val breadcrumbRecorderAtomic =
        atomic<BreadcrumbRecorder?>(null)
    private val crashReporterAtomic =
        atomic<SpectraCrashReporter?>(null)
    private val streamClientAtomic =
        atomic<com.spectra.logger.feature.streaming.SpectraStreamClient?>(null)
    private val activeScreenTimersLock = SynchronizedObject()
    private val activeScreenTimers = mutableMapOf<String, Pair<Instant, Map<String, String>>>()
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
                    breadcrumbRecorder = breadcrumbRecorder,
                    autoDetectSource = configuration.enabledFeatures.enableSourceDetection,
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

    /**
     * Event log storage instance.
     */
    public val eventStorage: EventLogStorage
        get() {
            var current = eventStorageAtomic.value
            if (current == null) {
                current = InMemoryEventLogStorage(maxCapacity = configuration.eventStorageConfig.maxCapacity)
                eventStorageAtomic.compareAndSet(null, current)
            }
            return eventStorageAtomic.value!!
        }

    /**
     * Crash storage instance.
     */
    public val crashStorage: CrashStorage
        get() {
            var current = crashStorageAtomic.value
            if (current == null) {
                current = InMemoryCrashStorage(maxCapacity = configuration.crashStorageConfig.maxCapacity)
                crashStorageAtomic.compareAndSet(null, current)
            }
            return crashStorageAtomic.value!!
        }

    /**
     * Breadcrumb recorder instance.
     */
    public val breadcrumbRecorder: BreadcrumbRecorder
        get() {
            var current = breadcrumbRecorderAtomic.value
            if (current == null) {
                current = DefaultBreadcrumbRecorder()
                breadcrumbRecorderAtomic.compareAndSet(null, current)
            }
            return breadcrumbRecorderAtomic.value!!
        }

    /**
     * Crash reporter instance.
     */
    public val crashReporter: SpectraCrashReporter
        get() {
            var current = crashReporterAtomic.value
            if (current == null) {
                current =
                    SpectraCrashReporter(
                        storage = crashStorage,
                        breadcrumbRecorder = breadcrumbRecorder,
                    )
                crashReporterAtomic.compareAndSet(null, current)
            }
            return crashReporterAtomic.value!!
        }

    /**
     * Remote streaming client managing WebSocket sync with desktop companions.
     */
    public val streamClient: com.spectra.logger.feature.streaming.SpectraStreamClient
        get() {
            var current = streamClientAtomic.value
            if (current == null) {
                current =
                    com.spectra.logger.feature.streaming.DefaultSpectraStreamClient(
                        logStorage = logStorage,
                        networkStorage = networkStorage,
                        eventStorage = eventStorage,
                        coroutineScope = ioScope,
                    )
                streamClientAtomic.compareAndSet(null, current)
            }
            return streamClientAtomic.value!!
        }

    /**
     * Connects to a remote desktop companion for real-time log streaming.
     */
    public suspend fun startStreaming(
        url: String,
        token: String,
    ) {
        streamClient.connect(url, token)
    }

    /**
     * Disconnects the active remote log streaming session.
     */
    public suspend fun stopStreaming() {
        streamClient.disconnect()
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
                        breadcrumbRecorder = breadcrumbRecorder,
                        autoDetectSource = configuration.enabledFeatures.enableSourceDetection,
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
     * Log a message with a dynamic [LogLevel].
     *
     * @param level Severity level
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception
     * @param metadata Optional context data
     */
    fun log(
        level: com.spectra.logger.feature.logs.model.LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        when (level) {
            com.spectra.logger.feature.logs.model.LogLevel.VERBOSE -> v(tag, message, throwable, metadata)
            com.spectra.logger.feature.logs.model.LogLevel.DEBUG -> d(tag, message, throwable, metadata)
            com.spectra.logger.feature.logs.model.LogLevel.INFO -> i(tag, message, throwable, metadata)
            com.spectra.logger.feature.logs.model.LogLevel.WARNING -> w(tag, message, throwable, metadata)
            com.spectra.logger.feature.logs.model.LogLevel.ERROR -> e(tag, message, throwable, metadata)
            com.spectra.logger.feature.logs.model.LogLevel.FATAL -> f(tag, message, throwable, metadata)
        }
    }

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
        breadcrumbRecorder.record(
            Breadcrumb(
                timestamp = entry.timestamp.toEpochMilliseconds(),
                type = BreadcrumbType.NETWORK,
                category = entry.method,
                message = "${entry.method} ${entry.url} [${entry.responseCode ?: 0}]",
                data = mapOf("duration" to entry.duration.toString()),
            ),
        )
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
     * Prune application logs according to retention policy.
     * @param policy Retention constraints (max count, max age TTL, max size).
     * @return Number of pruned entries.
     */
    suspend fun prune(policy: RetentionPolicy = RetentionPolicy.DEFAULT): Int = logger.prune(policy)

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

    /**
     * Prune network logs according to retention policy.
     */
    suspend fun pruneNetwork(policy: RetentionPolicy = RetentionPolicy.DEFAULT): Int = networkStorage.prune(policy)

    // Events Telemetry API

    /**
     * Log a user interaction, lifecycle, or custom event without blocking.
     */
    fun event(
        name: String,
        parameters: Map<String, String> = emptyMap(),
        eventType: EventType = EventType.USER_ACTION,
        durationMs: Long? = null,
    ) {
        if (!configuration.enabledFeatures.enableEventLogging) return
        val (source, sourceType) = SourceDetector.detectSource()
        val entry =
            EventLogEntry(
                id = IdGenerator.generate(),
                timestamp = SpectraTime.now(),
                eventType = eventType,
                name = name,
                parameters = parameters,
                durationMs = durationMs,
                source = source,
                sourceType = sourceType,
            )
        breadcrumbRecorder.record(
            Breadcrumb(
                timestamp = entry.timestamp.toEpochMilliseconds(),
                type = BreadcrumbType.EVENT,
                category = name,
                message = "Event: $name ($eventType)",
                data = parameters,
            ),
        )
        ioScope.launch {
            eventStorage.add(entry)
        }
    }

    /**
     * Mark the start of a screen transition or view session.
     * Begins tracking elapsed duration until [screenEnd] is called.
     */
    fun screenStart(
        screenName: String,
        parameters: Map<String, String> = emptyMap(),
    ) {
        if (!configuration.enabledFeatures.enableEventLogging) return
        val now = SpectraTime.now()
        synchronized(activeScreenTimersLock) {
            activeScreenTimers[screenName] = Pair(now, parameters)
        }
    }

    /**
     * Mark the end of a screen transition or view session.
     * Calculates elapsed duration and logs a [EventType.SCREEN_VIEW] event.
     */
    fun screenEnd(
        screenName: String,
        additionalParameters: Map<String, String> = emptyMap(),
    ) {
        if (!configuration.enabledFeatures.enableEventLogging) return
        val now = SpectraTime.now()
        val startInfo =
            synchronized(activeScreenTimersLock) {
                activeScreenTimers.remove(screenName)
            }
        val durationMs: Long? =
            startInfo?.let { (startTime, _) ->
                (now.toEpochMilliseconds() - startTime.toEpochMilliseconds()).coerceAtLeast(0L)
            }
        val mergedParams =
            if (startInfo != null) {
                startInfo.second + additionalParameters
            } else {
                additionalParameters
            }

        val (source, sourceType) = SourceDetector.detectSource()
        val entry =
            EventLogEntry(
                id = IdGenerator.generate(),
                timestamp = now,
                eventType = EventType.SCREEN_VIEW,
                name = screenName,
                parameters = mergedParams,
                durationMs = durationMs,
                source = source,
                sourceType = sourceType,
            )
        breadcrumbRecorder.record(
            Breadcrumb(
                timestamp = now.toEpochMilliseconds(),
                type = BreadcrumbType.EVENT,
                category = "ScreenView",
                message = "Screen: $screenName (${durationMs ?: 0}ms)",
                data = mergedParams,
            ),
        )
        ioScope.launch {
            eventStorage.add(entry)
        }
    }

    /**
     * Query stored event logs.
     */
    suspend fun queryEvents(
        filter: EventFilter = EventFilter.NONE,
        limit: Int? = null,
    ): List<EventLogEntry> = eventStorage.query(filter, limit)

    /**
     * Observe event logs as a coroutine flow.
     */
    fun observeEvents(filter: EventFilter = EventFilter.NONE): Flow<EventLogEntry> = eventStorage.observe(filter)

    /**
     * Get total event log count.
     */
    suspend fun eventCount(): Int = eventStorage.count()

    /**
     * Clear all events.
     */
    suspend fun clearEvents() = eventStorage.clear()

    /**
     * Prune analytics events according to retention policy.
     */
    suspend fun pruneEvents(policy: RetentionPolicy = RetentionPolicy.DEFAULT): Int = eventStorage.prune(policy)

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

        val currentEventStorage = eventStorage
        if (currentEventStorage is InMemoryEventLogStorage) {
            currentEventStorage.updateCapacity(newConfig.eventStorageConfig.maxCapacity)
        } else {
            eventStorageAtomic.value = InMemoryEventLogStorage(maxCapacity = newConfig.eventStorageConfig.maxCapacity)
        }

        val currentCrashStorage = crashStorageAtomic.value
        if (newConfig.crashStorageConfig.enablePersistence && newConfig.crashStorageConfig.directoryPath != null) {
            val fileSystem = FileSystem(newConfig.crashStorageConfig.directoryPath!!)
            crashStorageAtomic.value =
                FileCrashStorage(
                    fileSystem = fileSystem,
                    maxCrashes = newConfig.crashStorageConfig.maxCapacity,
                )
        } else if (currentCrashStorage == null) {
            crashStorageAtomic.value = InMemoryCrashStorage(maxCapacity = newConfig.crashStorageConfig.maxCapacity)
        }

        loggerAtomic.value =
            Logger(
                storage = logStorageAtomic.value!!,
                sinks = newConfig.logSinks,
                minLevel = newConfig.minLogLevel,
                scope = ioScope,
                breadcrumbRecorder = breadcrumbRecorder,
                autoDetectSource = newConfig.enabledFeatures.enableSourceDetection,
            )
    }

    // Crash Telemetry API

    /**
     * Record a manual breadcrumb leading up to any potential failure.
     */
    fun recordBreadcrumb(
        type: BreadcrumbType,
        category: String,
        message: String,
        data: Map<String, String> = emptyMap(),
    ) {
        breadcrumbRecorder.record(
            Breadcrumb(
                timestamp = SpectraTime.now().toEpochMilliseconds(),
                type = type,
                category = category,
                message = message,
                data = data,
            ),
        )
    }

    /**
     * Record a non-fatal exception without terminating the application.
     */
    fun recordNonFatalException(
        throwable: Throwable,
        metadata: Map<String, String> = emptyMap(),
    ) {
        crashReporter.recordNonFatalException(throwable, metadata)
    }

    /**
     * Query stored crash reports.
     */
    suspend fun queryCrashes(limit: Int? = null): List<CrashReport> = crashStorage.getCrashes(limit)

    /**
     * Get the most recent crash report, if any.
     */
    suspend fun getLatestCrash(): CrashReport? = crashStorage.getLatestCrash()

    /**
     * Observe crash reports as a flow.
     */
    fun observeCrashes(): Flow<List<CrashReport>> = crashStorage.observeCrashes()

    /**
     * Get total crash count.
     */
    suspend fun crashCount(): Int = crashStorage.count()

    /**
     * Installs the platform uncaught exception handler.
     */
    fun installCrashHandler() {
        crashReporter.install()
    }

    /**
     * Uninstalls the platform uncaught exception handler.
     */
    fun uninstallCrashHandler() {
        crashReporter.uninstall()
    }

    /**
     * Clear all recorded crash reports.
     */
    suspend fun clearCrashes() = crashStorage.clear()
}
