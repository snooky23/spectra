package com.spectra.logger.feature.events.storage

import com.spectra.logger.core.storage.TelemetryStorage
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventLogEntry
import kotlinx.coroutines.flow.Flow

/**
 * Storage interface for event entries.
 * Implementations must be thread-safe.
 */
interface EventLogStorage : TelemetryStorage<EventLogEntry, EventFilter> {
    /**
     * Retrieve event entries without filtering.
     * @param limit Maximum number of entries to return (null = no limit)
     */
    suspend fun query(limit: Int? = null): List<EventLogEntry> = query(EventFilter.NONE, limit)

    /**
     * Observe all event entries without filtering.
     */
    fun observe(): Flow<EventLogEntry> = observe(EventFilter.NONE)
}
