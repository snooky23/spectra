package com.spectra.logger.feature.network.statistics

import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.storage.NetworkLogStorage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Centralized, off-main-thread engine to aggregate raw network log data into structured statistical metrics.
 */
interface NetworkFilterEngineRepository {
    val statistics: StateFlow<NetworkDashboardStatistics>

    fun startObserving(scope: CoroutineScope)

    fun stopObserving()
}

class NetworkFilterEngineRepositoryImpl(
    private val networkStorage: NetworkLogStorage,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NetworkFilterEngineRepository {

    private val _statistics = MutableStateFlow(
        NetworkDashboardStatistics(
            totalRequests = 0,
            statusCounts = persistentMapOf(
                "2xx" to 0,
                "3xx" to 0,
                "4xx" to 0,
                "5xx" to 0,
                "Failed" to 0
            ),
            latencyDistribution = persistentMapOf(
                "< 100ms" to 0,
                "100-500ms" to 0,
                "500ms-2s" to 0,
                "> 2s" to 0
            ),
            timeline = persistentListOf()
        )
    )
    override val statistics: StateFlow<NetworkDashboardStatistics> = _statistics.asStateFlow()

    private var observationJob: Job? = null

    override fun startObserving(scope: CoroutineScope) {
        if (observationJob?.isActive == true) return

        observationJob = scope.launch(dispatcher) {
            // Initial load
            val initialLogs = networkStorage.query()
            var currentStats = aggregateLogs(initialLogs)
            _statistics.value = currentStats

            // Observe new logs
            networkStorage.observe().collect { log ->
                currentStats = addLogToStatistics(currentStats, log)
                _statistics.value = currentStats
            }
        }
    }

    override fun stopObserving() {
        observationJob?.cancel()
        observationJob = null
    }

    private fun aggregateLogs(logs: List<NetworkLogEntry>): NetworkDashboardStatistics {
        val statusCounts = mutableMapOf(
            "2xx" to 0,
            "3xx" to 0,
            "4xx" to 0,
            "5xx" to 0,
            "Failed" to 0
        )
        val latencyDistribution = mutableMapOf(
            "< 100ms" to 0,
            "100-500ms" to 0,
            "500ms-2s" to 0,
            "> 2s" to 0
        )

        // Map of bucketTimestamp -> (count, totalDurationMs)
        val timelineMap = mutableMapOf<Long, Pair<Int, Long>>()

        logs.forEach { log ->
            val statusCategory = getStatusCategory(log)
            statusCounts[statusCategory] = (statusCounts[statusCategory] ?: 0) + 1

            val latencyCategory = getLatencyCategory(log.duration)
            latencyDistribution[latencyCategory] = (latencyDistribution[latencyCategory] ?: 0) + 1

            val bucket = getBucketTimestamp(log.timestamp.toEpochMilliseconds())
            val existing = timelineMap[bucket] ?: Pair(0, 0L)
            timelineMap[bucket] = Pair(existing.first + 1, existing.second + log.duration)
        }

        val timelineBuckets = timelineMap.entries
            .sortedBy { it.key }
            .map { (timestamp, data) ->
                val (count, totalDuration) = data
                NetworkTimelineBucket(
                    timestamp = timestamp,
                    count = count,
                    averageDurationMs = if (count > 0) totalDuration / count else 0L
                )
            }.toPersistentList()

        return NetworkDashboardStatistics(
            totalRequests = logs.size,
            statusCounts = statusCounts.toPersistentMap(),
            latencyDistribution = latencyDistribution.toPersistentMap(),
            timeline = timelineBuckets
        )
    }

    private fun addLogToStatistics(
        current: NetworkDashboardStatistics,
        log: NetworkLogEntry,
    ): NetworkDashboardStatistics {
        val newTotal = current.totalRequests + 1

        // Update status counts
        val statusCategory = getStatusCategory(log)
        val currentStatusCount = current.statusCounts[statusCategory] ?: 0
        val newStatusCounts = current.statusCounts.put(statusCategory, currentStatusCount + 1)

        // Update latency counts
        val latencyCategory = getLatencyCategory(log.duration)
        val currentLatencyCount = current.latencyDistribution[latencyCategory] ?: 0
        val newLatencyDistribution = current.latencyDistribution.put(latencyCategory, currentLatencyCount + 1)

        // Update timeline
        val bucketTimestamp = getBucketTimestamp(log.timestamp.toEpochMilliseconds())
        val newTimeline = current.timeline.toMutableList()
        val existingIndex = newTimeline.indexOfFirst { it.timestamp == bucketTimestamp }

        if (existingIndex != -1) {
            val bucket = newTimeline[existingIndex]
            val newCount = bucket.count + 1
            val totalDuration = bucket.averageDurationMs * bucket.count + log.duration
            val newAvg = totalDuration / newCount
            newTimeline[existingIndex] = bucket.copy(count = newCount, averageDurationMs = newAvg)
        } else {
            val newBucket = NetworkTimelineBucket(
                timestamp = bucketTimestamp,
                count = 1,
                averageDurationMs = log.duration
            )
            newTimeline.add(newBucket)
            newTimeline.sortBy { it.timestamp }
        }

        return NetworkDashboardStatistics(
            totalRequests = newTotal,
            statusCounts = newStatusCounts,
            latencyDistribution = newLatencyDistribution,
            timeline = newTimeline.toPersistentList()
        )
    }

    private fun getStatusCategory(log: NetworkLogEntry): String {
        if (log.error != null || log.responseCode == null) return "Failed"
        return when (log.responseCode) {
            in 200..299 -> "2xx"
            in 300..399 -> "3xx"
            in 400..499 -> "4xx"
            in 500..599 -> "5xx"
            else -> "Failed"
        }
    }

    private fun getLatencyCategory(duration: Long): String {
        return when {
            duration < 100 -> "< 100ms"
            duration in 100..499 -> "100-500ms"
            duration in 500..1999 -> "500ms-2s"
            else -> "> 2s"
        }
    }

    private fun getBucketTimestamp(timestamp: Long): Long {
        // Group by 1-minute intervals (60,000 ms)
        val bucketSize = 60_000L
        return (timestamp / bucketSize) * bucketSize
    }
}
