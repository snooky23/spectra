package com.spectra.logger.feature.logs.statistics

import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.feature.logs.model.LogLevel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

/**
 * Immutable data structure representing the aggregated statistics for the dashboard.
 */
data class DashboardStatistics(
    val timeline: PersistentList<TimelineBucket>,
    val levelCounts: PersistentMap<LogLevel, Int>,
    val tagCounts: PersistentMap<String, Int>,
    val totalLogs: Int,
)

/**
 * Represents a single time bucket in the dashboard timeline graph.
 */
data class TimelineBucket(
    val timestamp: Long,
    val counts: PersistentMap<LogLevel, Int>,
)
