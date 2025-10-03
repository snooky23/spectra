package com.spectra.logger

import com.spectra.logger.domain.Logger
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.InMemoryLogStorage
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for the Spectra Logger framework.
 * Provides a simple, global API for logging.
 *
 * @since 0.0.1
 */
object SpectraLogger {
    private val logger =
        Logger(
            storage = InMemoryLogStorage(),
            minLevel = LogLevel.VERBOSE,
        )

    /**
     * Returns the current version of the Spectra Logger framework.
     */
    fun getVersion(): String = "0.0.1-SNAPSHOT"

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
}
