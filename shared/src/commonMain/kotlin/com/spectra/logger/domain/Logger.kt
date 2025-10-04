package com.spectra.logger.domain

import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.utils.IdGenerator
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
     */
    fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        log(LogLevel.VERBOSE, tag, message, throwable, metadata)
    }

    /**
     * Log a debug message.
     */
    fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        log(LogLevel.DEBUG, tag, message, throwable, metadata)
    }

    /**
     * Log an info message.
     */
    fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        log(LogLevel.INFO, tag, message, throwable, metadata)
    }

    /**
     * Log a warning message.
     */
    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        log(LogLevel.WARNING, tag, message, throwable, metadata)
    }

    /**
     * Log an error message.
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        log(LogLevel.ERROR, tag, message, throwable, metadata)
    }

    /**
     * Log a fatal error message.
     */
    fun f(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        log(LogLevel.FATAL, tag, message, throwable, metadata)
    }

    /**
     * Core log function. Thread-safe with async storage.
     */
    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        if (level.priority < minLevel.priority) return

        val entry =
            LogEntry(
                id = IdGenerator.generate(),
                timestamp = Clock.System.now(),
                level = level,
                tag = tag,
                message = message,
                throwable = throwable?.stackTraceToString(),
                metadata = metadata,
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
