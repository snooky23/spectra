package com.spectra.logger.feature.events.storage

import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventLogEntry
import kotlinx.coroutines.flow.Flow

/**
 * Storage interface for event entries.
 * Implementations must be thread-safe.
 */
interface EventLogStorage {
    /**
     * Add an event entry to storage.
     */
    suspend fun add(entry: EventLogEntry)

    /**
     * Add multiple event entries to storage.
     */
    suspend fun addAll(entries: List<EventLogEntry>)

    /**
     * Retrieve event entries matching the filter.
     * @param filter Filter criteria
     * @param limit Maximum number of entries to return (null = no limit)
     * @return List of matching event entries, sorted by timestamp descending
     */
    suspend fun query(
        filter: EventFilter = EventFilter.NONE,
        limit: Int? = null,
    ): List<EventLogEntry>

    /**
     * Observe event entries as a coroutine flow.
     */
    fun observe(filter: EventFilter = EventFilter.NONE): Flow<EventLogEntry>

    /**
     * Get total count of stored events.
     */
    suspend fun count(): Int

    /**
     * Clear all event entries from storage.
     */
    suspend fun clear()
}
