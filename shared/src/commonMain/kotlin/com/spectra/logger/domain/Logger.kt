package com.spectra.logger.domain

import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.utils.IdGenerator
import com.spectra.logger.utils.SourceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Core logger implementation.
 * Thread-safe with asynchronous log capture.
 *
 * @property storage Log storage implementation
 * @property minLevel Minimum log level to capture (default: VERBOSE)
 * @property scope Coroutine scope for async operations (default: background scope)
 */
class Logger(
    private val storage: LogStorage,
    private val minLevel: LogLevel = LogLevel.VERBOSE,
    scope: CoroutineScope? = null,
) {
    private val scope: CoroutineScope = scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Log a verbose message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (nullable, follows industry standard)
     */
    fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        log(LogLevel.VERBOSE, tag, message, throwable, metadata)
    }

    /**
     * Log a debug message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (nullable, follows industry standard)
     */
    fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        log(LogLevel.DEBUG, tag, message, throwable, metadata)
    }

    /**
     * Log an info message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (nullable, follows industry standard)
     */
    fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        log(LogLevel.INFO, tag, message, throwable, metadata)
    }

    /**
     * Log a warning message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (nullable, follows industry standard)
     */
    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        log(LogLevel.WARNING, tag, message, throwable, metadata)
    }

    /**
     * Log an error message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (nullable, follows industry standard)
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        log(LogLevel.ERROR, tag, message, throwable, metadata)
    }

    /**
     * Log a fatal error message.
     *
     * @param tag Category or source of the log
     * @param message The log message
     * @param throwable Optional exception (for error logging)
     * @param metadata Optional context data (nullable, follows industry standard)
     */
    fun f(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        log(LogLevel.FATAL, tag, message, throwable, metadata)
    }

    /**
     * Core log function. Thread-safe with async storage.
     * Automatically detects the source (app, SDK, or plugin) from the call stack.
     *
     * Converts null metadata to empty map internally (following industry standard pattern).
     */
    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        if (level.priority < minLevel.priority) return

        val (source, sourceType) = SourceDetector.detectSource()

        // Convert null to empty map (industry standard pattern from Firebase, Sentry, etc)
        val logMetadata = metadata ?: emptyMap()

        val entry =
            LogEntry(
                id = IdGenerator.generate(),
                timestamp = Clock.System.now(),
                level = level,
                tag = tag,
                message = message,
                throwable = throwable?.stackTraceToString(),
                metadata = logMetadata,
                source = source,
                sourceType = sourceType,
            )

        scope.launch {
            storage.add(entry)
        }
    }

    /**
     * Query stored logs.
     */
    suspend fun query(
        filter: LogFilter = LogFilter.NONE,
        limit: Int? = null,
    ): List<LogEntry> = storage.query(filter, limit)

    /**
     * Observe logs as a flow.
     */
    fun observe(filter: LogFilter = LogFilter.NONE): Flow<LogEntry> = storage.observe(filter)

    /**
     * Get total log count.
     */
    suspend fun count(): Int = storage.count()

    /**
     * Clear all logs.
     */
    suspend fun clear() = storage.clear()
}
