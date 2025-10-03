package com.spectra.logger.domain.storage

import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.model.NetworkLogFilter
import kotlinx.coroutines.flow.Flow

/**
 * Storage interface for network log entries.
 * Implementations must be thread-safe.
 */
interface NetworkLogStorage {
    /**
     * Add a network log entry to storage.
     * @param entry The network log entry to store
     */
    suspend fun add(entry: NetworkLogEntry)

    /**
     * Retrieve network log entries matching the filter.
     * @param filter Filter criteria
     * @param limit Maximum number of entries to return (null = no limit)
     * @return List of matching network log entries, sorted by timestamp descending
     */
    suspend fun query(
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
        limit: Int? = null,
    ): List<NetworkLogEntry>

    /**
     * Observe network log entries as a flow.
     * @param filter Filter criteria
     * @return Flow of network log entries matching the filter
     */
    fun observe(filter: NetworkLogFilter = NetworkLogFilter.NONE): Flow<NetworkLogEntry>

    /**
     * Get total count of stored entries.
     */
    suspend fun count(): Int

    /**
     * Clear all network log entries from storage.
     */
    suspend fun clear()
}
