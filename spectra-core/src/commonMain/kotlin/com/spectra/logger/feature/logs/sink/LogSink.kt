package com.spectra.logger.feature.logs.sink

import com.spectra.logger.feature.logs.model.LogEntry

/**
 * Interface for intercepting and handling standard logs captured by Spectra Logger.
 *
 * Sinks are isolated plugins that receive logs asynchronously. If a Sink throws an exception,
 * it will be caught and swallowed by the Logger to ensure the host application does not crash.
 */
interface LogSink {
    /**
     * Called whenever a new standard log is recorded.
     * @param entry The log entry containing message, level, metadata, and stacktrace.
     */
    suspend fun log(entry: LogEntry)
}
