package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.storage.TelemetryStorage
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogFilter
import kotlinx.coroutines.flow.Flow

/**
 * Storage interface for log entries.
 * Implementations must be thread-safe.
 */
interface LogStorage : TelemetryStorage<LogEntry, LogFilter> {
    /**
     * Retrieve log entries without filtering.
     * @param limit Maximum number of entries to return (null = no limit)
     */
    suspend fun query(limit: Int? = null): List<LogEntry> = query(LogFilter.NONE, limit)

    /**
     * Observe all log entries without filtering.
     */
    fun observe(): Flow<LogEntry> = observe(LogFilter.NONE)

    /**
     * Export all log entries to a single file and return its absolute path.
     * @return Absolute path to the exported `.jsonl` file, or null if empty/failed.
     */
    suspend fun exportLogs(): String?
}
