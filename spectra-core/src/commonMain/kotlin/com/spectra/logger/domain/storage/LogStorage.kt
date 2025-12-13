package com.spectra.logger.domain.storage

import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import kotlinx.coroutines.flow.Flow

/**
 * Storage interface for log entries.
 * Implementations must be thread-safe.
 */
interface LogStorage {
    /**
     * Add a log entry to storage.
     * @param entry The log entry to store
     */
    suspend fun add(entry: LogEntry)

    /**
     * Add multiple log entries to storage.
     * @param entries The log entries to store
     */
    suspend fun addAll(entries: List<LogEntry>)

    /**
     * Retrieve log entries matching the filter.
     * @param filter Filter criteria
     * @param limit Maximum number of entries to return (null = no limit)
     * @return List of matching log entries, sorted by timestamp descending
     */
    suspend fun query(
        filter: LogFilter = LogFilter.NONE,
        limit: Int? = null,
    ): List<LogEntry>

    /**
     * Observe log entries as a flow.
     * @param filter Filter criteria
     * @return Flow of log entries matching the filter
     */
    fun observe(filter: LogFilter = LogFilter.NONE): Flow<LogEntry>

    /**
     * Get total count of stored entries.
     */
    suspend fun count(): Int

    /**
     * Clear all log entries from storage.
     */
    suspend fun clear()
}
