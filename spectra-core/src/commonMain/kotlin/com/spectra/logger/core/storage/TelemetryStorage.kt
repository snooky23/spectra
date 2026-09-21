package com.spectra.logger.core.storage

import kotlinx.coroutines.flow.Flow

/**
 * Generic contract for telemetry storage engines in Spectra Logger.
 *
 * Implements clean architecture principles with low coupling and high cohesion
 * across logging, network telemetry, analytics events, and debug sessions.
 *
 * @param T The telemetry model entity (e.g., LogEntry, NetworkLogEntry, EventLogEntry)
 * @param F The query filter criteria type (e.g., LogFilter, NetworkLogFilter, EventFilter)
 */
interface TelemetryStorage<T, in F> {
    /**
     * Store a telemetry item asynchronously.
     */
    suspend fun add(entry: T)

    /**
     * Store multiple telemetry items asynchronously.
     */
    suspend fun addAll(entries: List<T>) {
        entries.forEach { add(it) }
    }

    /**
     * Query telemetry items matching the filter.
     */
    suspend fun query(
        filter: F,
        limit: Int? = null,
    ): List<T>

    /**
     * Observe incoming telemetry items matching the filter as a coroutine flow.
     */
    fun observe(filter: F): Flow<T>

    /**
     * Get total count of stored items.
     */
    suspend fun count(): Int

    /**
     * Clear all stored items.
     */
    suspend fun clear()

    /**
     * Prune stored items according to the given retention policy.
     * @param policy Retention constraints (max count, max age TTL, max size).
     * @return Number of pruned entries.
     */
    suspend fun prune(policy: RetentionPolicy): Int = 0
}
