package com.spectra.logger.feature.network.statistics

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

/**
 * Immutable data structure representing the aggregated network statistics.
 */
data class NetworkDashboardStatistics(
    val totalRequests: Int,
    val statusCounts: PersistentMap<String, Int>, // "2xx", "3xx", "4xx", "5xx", "Failed"
    val latencyDistribution: PersistentMap<String, Int>, // "< 100ms", "100-500ms", "500ms-2s", "> 2s"
    val timeline: PersistentList<NetworkTimelineBucket>,
)

/**
 * Represents a timeline bucket for network statistics.
 */
data class NetworkTimelineBucket(
    val timestamp: Long,
    val count: Int,
    val averageDurationMs: Long,
)
