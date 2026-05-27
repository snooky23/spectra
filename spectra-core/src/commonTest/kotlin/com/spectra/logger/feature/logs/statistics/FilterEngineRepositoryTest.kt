package com.spectra.logger.feature.logs.statistics

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import app.cash.turbine.test
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.logs.storage.InMemoryLogStorage
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterEngineRepositoryTest {
    private fun createEntry(
        id: String,
        timestamp: Long,
        level: LogLevel = LogLevel.INFO,
    ) = LogEntry(
        id = id,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        level = level,
        tag = "Test",
        message = "Message $id",
    )

    @Test
    fun testStatisticsAggregation() =
        runTest {
            val storage = InMemoryLogStorage()
            val repository = FilterEngineRepositoryImpl(storage)

            // Seed some data
            storage.add(createEntry("1", 1000L, LogLevel.INFO))
            storage.add(createEntry("2", 1500L, LogLevel.ERROR))
            storage.add(createEntry("3", 65000L, LogLevel.INFO)) // Next bucket (1 min = 60000ms)

            repository.startObserving(this)

            repository.statistics.test {
                // Skip the initial empty state
                val initialState = awaitItem()
                assertEquals(0, initialState.totalLogs)

                // Wait for the initially seeded logs to be processed
                val stats = awaitItem()

                assertEquals(3, stats.totalLogs)
                assertEquals(2, stats.levelCounts[LogLevel.INFO])
                assertEquals(1, stats.levelCounts[LogLevel.ERROR])

                // Bucket 1 (0ms) has 2 logs
                assertEquals(0L, stats.timeline[0].timestamp)
                assertEquals(1, stats.timeline[0].counts[LogLevel.INFO])
                assertEquals(1, stats.timeline[0].counts[LogLevel.ERROR])

                // Bucket 2 (60000ms) has 1 log
                assertEquals(60000L, stats.timeline[1].timestamp)
                assertEquals(1, stats.timeline[1].counts[LogLevel.INFO])

                // Add new log dynamically
                storage.add(createEntry("4", 120000L, LogLevel.WARNING)) // Next next bucket

                val newStats = awaitItem()
                assertEquals(4, newStats.totalLogs)
                assertEquals(1, newStats.levelCounts[LogLevel.WARNING])
                assertEquals(3, newStats.timeline.size)
                assertEquals(120000L, newStats.timeline[2].timestamp)
                assertEquals(1, newStats.timeline[2].counts[LogLevel.WARNING])

                cancelAndIgnoreRemainingEvents()
            }

            repository.stopObserving()
        }
}
