package com.spectra.logger.domain.model

/**
 * Filter criteria for querying log entries.
 *
 * @property levels Filter by log levels (null = all levels)
 * @property tags Filter by tags (null = all tags)
 * @property searchText Filter by message content (null = no text filtering)
 * @property fromTimestamp Filter logs after this time (null = no start limit)
 * @property toTimestamp Filter logs before this time (null = no end limit)
 * @property metadataKey Filter logs containing this metadata key
 * @property metadataValue Filter logs where the metadata key matches this value
 */
data class LogFilter(
    val levels: Set<LogLevel>? = null,
    val tags: Set<String>? = null,
    val searchText: String? = null,
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null,
    val metadataKey: String? = null,
    val metadataValue: String? = null,
) {
    /**
     * Checks if a log entry matches this filter.
     */
    fun matches(entry: LogEntry): Boolean {
        if (levels != null && entry.level !in levels) return false
        if (tags != null && entry.tag !in tags) return false
        if (searchText != null && !entry.message.contains(searchText, ignoreCase = true)) return false
        if (fromTimestamp != null && entry.timestamp.toEpochMilliseconds() < fromTimestamp) return false
        if (toTimestamp != null && entry.timestamp.toEpochMilliseconds() > toTimestamp) return false
        
        if (metadataKey != null) {
            val value = entry.metadata[metadataKey] ?: return false
            if (metadataValue != null && value != metadataValue) return false
        }
        
        return true
    }

    companion object {
        /**
         * Creates a filter that matches all entries.
         */
        val NONE = LogFilter()
    }
}
