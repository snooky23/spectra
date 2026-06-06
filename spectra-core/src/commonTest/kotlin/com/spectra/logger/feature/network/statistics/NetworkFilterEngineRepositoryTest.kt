package com.spectra.logger.feature.network.statistics

import app.cash.turbine.test
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.storage.InMemoryNetworkLogStorage
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkFilterEngineRepositoryTest {
    private fun createEntry(
        id: String,
        timestamp: Long,
        duration: Long,
        responseCode: Int? = 200,
        error: String? = null,
    ) = NetworkLogEntry(
        id = id,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        url = "https://example.com/api/$id",
        method = "GET",
        duration = duration,
        responseCode = responseCode,
        error = error,
    )

    @Test
    fun testNetworkStatisticsAggregation() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryNetworkLogStorage()
            val repository = NetworkFilterEngineRepositoryImpl(storage, UnconfinedTestDispatcher())

            // Seed some data
            storage.add(createEntry("1", 1000L, 50L, 200)) // 2xx, <100ms
            storage.add(createEntry("2", 1500L, 250L, 404)) // 4xx, 100-500ms
            storage.add(createEntry("3", 65000L, 1200L, 500)) // 5xx, 500ms-2s (next bucket)
            storage.add(createEntry("4", 66000L, 3000L, null, "Timeout")) // Failed, >2s

            try {
                repository.startObserving(backgroundScope)

                repository.statistics.test {
                    // With UnconfinedTestDispatcher, the initially seeded logs are processed immediately
                    val stats = awaitItem()
                    assertEquals(4, stats.totalRequests)
                    assertEquals(1, stats.statusCounts["2xx"])
                    assertEquals(1, stats.statusCounts["4xx"])
                    assertEquals(1, stats.statusCounts["5xx"])
                    assertEquals(1, stats.statusCounts["Failed"])

                    assertEquals(1, stats.latencyDistribution["< 100ms"])
                    assertEquals(1, stats.latencyDistribution["100-500ms"])
                    assertEquals(1, stats.latencyDistribution["500ms-2s"])
                    assertEquals(1, stats.latencyDistribution["> 2s"])

                    // Bucket 1 (0ms) has 2 logs with durations 50 and 250 -> average = 150
                    assertEquals(0L, stats.timeline[0].timestamp)
                    assertEquals(2, stats.timeline[0].count)
                    assertEquals(150L, stats.timeline[0].averageDurationMs)

                    // Bucket 2 (60000ms) has 2 logs with durations 1200 and 3000 -> average = 2100
                    assertEquals(60000L, stats.timeline[1].timestamp)
                    assertEquals(2, stats.timeline[1].count)
                    assertEquals(2100L, stats.timeline[1].averageDurationMs)

                    // Add new log dynamically
                    storage.add(createEntry("5", 125000L, 80L, 302)) // 3xx, <100ms, next next bucket

                    val newStats = awaitItem()
                    assertEquals(5, newStats.totalRequests)
                    assertEquals(1, newStats.statusCounts["3xx"])
                    assertEquals(3, newStats.timeline.size)
                    assertEquals(120000L, newStats.timeline[2].timestamp)
                    assertEquals(1, newStats.timeline[2].count)
                    assertEquals(80L, newStats.timeline[2].averageDurationMs)

                    cancelAndIgnoreRemainingEvents()
                }
            } finally {
                repository.stopObserving()
            }
        }
}
