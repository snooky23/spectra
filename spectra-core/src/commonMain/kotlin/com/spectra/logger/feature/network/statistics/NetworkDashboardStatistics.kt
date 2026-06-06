package com.spectra.logger.feature.network.statistics

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

/**
 * Immutable data structure representing the aggregated network statistics.
 */
data class NetworkDashboardStatistics(
    val totalRequests: Int,
    // "2xx", "3xx", "4xx", "5xx", "Failed"
    val statusCounts: PersistentMap<String, Int>,
    // "< 100ms", "100-500ms", "500ms-2s", "> 2s"
    val latencyDistribution: PersistentMap<String, Int>,
    val timeline: PersistentList<NetworkTimelineBucket>,
)

/**
 * Represents a timeline bucket for network statistics.
 */
data class NetworkTimelineBucket(
    val timestamp: Long,
    val count: Int,
    val totalDurationMs: Long,
) {
    val averageDurationMs: Long
        get() = if (count > 0) totalDurationMs / count else 0L
}
