package com.spectra.logger.domain.statistics

import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.LogStorage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Centralized, off-main-thread engine to aggregate raw log data into structured statistical metrics.
 */
interface FilterEngineRepository {
    val statistics: StateFlow<DashboardStatistics>

    fun startObserving(scope: CoroutineScope)

    fun stopObserving()
}

class FilterEngineRepositoryImpl(
    private val logStorage: LogStorage,
) : FilterEngineRepository {
    private val _statistics =
        MutableStateFlow(
            DashboardStatistics(
                timeline = persistentListOf(),
                levelCounts = persistentMapOf(),
                totalLogs = 0,
            ),
        )
    override val statistics: StateFlow<DashboardStatistics> = _statistics.asStateFlow()

    private var observationJob: Job? = null

    override fun startObserving(scope: CoroutineScope) {
        if (observationJob?.isActive == true) return

        observationJob =
            scope.launch(Dispatchers.Default) {
                // Initial load
                val initialLogs = logStorage.query()
                var currentStats = aggregateLogs(initialLogs)
                _statistics.value = currentStats

                // Observe new logs
                logStorage.observe().collect { log ->
                    currentStats = addLogToStatistics(currentStats, log)
                    _statistics.value = currentStats
                }
            }
    }

    override fun stopObserving() {
        observationJob?.cancel()
        observationJob = null
    }

    private fun aggregateLogs(logs: List<LogEntry>): DashboardStatistics {
        val levelCounts = mutableMapOf<LogLevel, Int>()
        val timelineMap = mutableMapOf<Long, MutableMap<LogLevel, Int>>()

        logs.forEach { log ->
            levelCounts[log.level] = (levelCounts[log.level] ?: 0) + 1

            val bucket = getBucketTimestamp(log.timestamp.toEpochMilliseconds())
            val bucketCounts = timelineMap.getOrPut(bucket) { mutableMapOf() }
            bucketCounts[log.level] = (bucketCounts[log.level] ?: 0) + 1
        }

        val timelineBuckets =
            timelineMap.entries.sortedBy { it.key }.map { (timestamp, counts) ->
                TimelineBucket(
                    timestamp = timestamp,
                    counts = counts.toPersistentMap(),
                )
            }.toPersistentList()

        return DashboardStatistics(
            timeline = timelineBuckets,
            levelCounts = levelCounts.toPersistentMap(),
            totalLogs = logs.size,
        )
    }

    private fun addLogToStatistics(
        current: DashboardStatistics,
        log: LogEntry,
    ): DashboardStatistics {
        val newTotal = current.totalLogs + 1

        // Update level counts
        val currentLevelCount = current.levelCounts[log.level] ?: 0
        val newLevelCounts = current.levelCounts.put(log.level, currentLevelCount + 1)

        // Update timeline
        val bucketTimestamp = getBucketTimestamp(log.timestamp.toEpochMilliseconds())

        val newTimeline = current.timeline.toMutableList()
        val existingBucketIndex = newTimeline.indexOfFirst { it.timestamp == bucketTimestamp }

        if (existingBucketIndex != -1) {
            val bucket = newTimeline[existingBucketIndex]
            val count = bucket.counts[log.level] ?: 0
            val newCounts = bucket.counts.put(log.level, count + 1)
            newTimeline[existingBucketIndex] = bucket.copy(counts = newCounts)
        } else {
            val newCounts = persistentMapOf(log.level to 1)
            val newBucket = TimelineBucket(timestamp = bucketTimestamp, counts = newCounts)
            newTimeline.add(newBucket)
            newTimeline.sortBy { it.timestamp }
        }

        return DashboardStatistics(
            timeline = newTimeline.toPersistentList(),
            levelCounts = newLevelCounts,
            totalLogs = newTotal,
        )
    }

    private fun getBucketTimestamp(timestamp: Long): Long {
        // Group by 1-minute intervals (60,000 ms)
        val bucketSize = 60_000L
        return (timestamp / bucketSize) * bucketSize
    }
}
