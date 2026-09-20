package com.spectra.logger.feature.events.model

/**
 * Filter criteria for querying event logs.
 *
 * @property eventTypes Filter by event types (null = all types)
 * @property searchText Filter by event name or parameter values (null = no text filtering)
 * @property minDurationMs Filter events with at least this duration in milliseconds
 * @property fromTimestamp Filter events occurring after this epoch timestamp in milliseconds
 * @property toTimestamp Filter events occurring before this epoch timestamp in milliseconds
 */
data class EventFilter(
    val eventTypes: Set<EventType>? = null,
    val searchText: String? = null,
    val minDurationMs: Long? = null,
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null,
) {
    /**
     * Checks if an event matches this filter.
     */
    fun matches(entry: EventLogEntry): Boolean {
        if (eventTypes != null && entry.eventType !in eventTypes) return false
        if (minDurationMs != null && (entry.durationMs == null || entry.durationMs < minDurationMs)) return false
        if (fromTimestamp != null && entry.timestamp.toEpochMilliseconds() < fromTimestamp) return false
        if (toTimestamp != null && entry.timestamp.toEpochMilliseconds() > toTimestamp) return false

        if (searchText != null && searchText.isNotBlank()) {
            val query = searchText.trim()
            val matchesName = entry.name.contains(query, ignoreCase = true)
            val matchesParams =
                entry.parameters.any { (k, v) ->
                    k.contains(query, ignoreCase = true) || v.contains(query, ignoreCase = true)
                }
            if (!matchesName && !matchesParams) return false
        }

        return true
    }

    companion object {
        /**
         * Creates a filter matching all event entries.
         */
        val NONE = EventFilter()
    }
}
