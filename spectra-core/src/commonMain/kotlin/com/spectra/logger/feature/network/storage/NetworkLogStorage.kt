package com.spectra.logger.feature.network.storage

import com.spectra.logger.core.storage.TelemetryStorage
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.model.NetworkLogFilter
import kotlinx.coroutines.flow.Flow

/**
 * Storage interface for network log entries.
 * Implementations must be thread-safe.
 */
interface NetworkLogStorage : TelemetryStorage<NetworkLogEntry, NetworkLogFilter> {
    /**
     * Retrieve network log entries without filtering.
     * @param limit Maximum number of entries to return (null = no limit)
     */
    suspend fun query(limit: Int? = null): List<NetworkLogEntry> = query(NetworkLogFilter.NONE, limit)

    /**
     * Observe all network log entries without filtering.
     */
    fun observe(): Flow<NetworkLogEntry> = observe(NetworkLogFilter.NONE)
}
