package com.spectra.logger.core.storage

import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.events.storage.InMemoryEventLogStorage
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.logs.storage.InMemoryLogStorage
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.storage.InMemoryNetworkLogStorage
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RetentionPolicyTest {
    @Test
    fun testRetentionPolicyPresets() {
        assertTrue(RetentionPolicy.DEFAULT.hasLimits)
        assertEquals(10_000, RetentionPolicy.DEFAULT.maxCount)
        assertEquals(RetentionPolicy.SEVEN_DAYS_MS, RetentionPolicy.DEFAULT.maxAgeMs)

        assertFalse(RetentionPolicy.NONE.hasLimits)
        assertTrue(RetentionPolicy.SEVEN_DAYS.hasLimits)
        assertTrue(RetentionPolicy.THIRTY_DAYS.hasLimits)
        assertTrue(RetentionPolicy.STRICT_MEMORY.hasLimits)
    }

    @Test
    fun testInMemoryLogStoragePruneByCount() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100)
            val now = SpectraTime.now()

            for (i in 1..20) {
                storage.add(
                    LogEntry(
                        id = "log_$i",
                        timestamp = now,
                        level = LogLevel.INFO,
                        tag = "Test",
                        message = "Message $i",
                    ),
                )
            }
            assertEquals(20, storage.count())

            val pruned = storage.prune(RetentionPolicy(maxCount = 5))
            assertEquals(15, pruned)
            assertEquals(5, storage.count())

            val remaining = storage.query()
            assertEquals(5, remaining.size)
            // Ring buffer query() returns newest first
            assertEquals("log_20", remaining.first().id)
            assertEquals("log_16", remaining.last().id)
        }

    @Test
    fun testInMemoryLogStoragePruneByTtl() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100)
            val now = SpectraTime.now()
            val oldTimestamp = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - 100_000L)

            // Add 5 old logs
            for (i in 1..5) {
                storage.add(
                    LogEntry(
                        id = "old_$i",
                        timestamp = oldTimestamp,
                        level = LogLevel.INFO,
                        tag = "Old",
                        message = "Old message $i",
                    ),
                )
            }
            // Add 5 fresh logs
            for (i in 6..10) {
                storage.add(
                    LogEntry(
                        id = "new_$i",
                        timestamp = now,
                        level = LogLevel.INFO,
                        tag = "New",
                        message = "New message $i",
                    ),
                )
            }
            assertEquals(10, storage.count())

            // Prune items older than 50 seconds (50_000ms)
            val pruned = storage.prune(RetentionPolicy(maxAgeMs = 50_000L))
            assertEquals(5, pruned)
            assertEquals(5, storage.count())

            val remaining = storage.query()
            assertTrue(remaining.all { it.id.startsWith("new_") })
        }

    @Test
    fun testInMemoryNetworkLogStoragePrune() =
        runTest {
            val storage = InMemoryNetworkLogStorage(maxCapacity = 100)
            val now = SpectraTime.now()
            val oldTime = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - 200_000L)

            for (i in 1..10) {
                storage.add(
                    NetworkLogEntry(
                        id = "req_$i",
                        timestamp = if (i <= 4) oldTime else now,
                        url = "https://api.example.com/$i",
                        method = "GET",
                    ),
                )
            }
            assertEquals(10, storage.count())

            val pruned = storage.prune(RetentionPolicy(maxAgeMs = 100_000L))
            assertEquals(4, pruned)
            assertEquals(6, storage.count())
        }

    @Test
    fun testInMemoryEventLogStoragePrune() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 100)
            val now = SpectraTime.now()

            for (i in 1..15) {
                storage.add(
                    EventLogEntry(
                        id = "evt_$i",
                        timestamp = now,
                        eventType = EventType.CUSTOM,
                        name = "event_$i",
                    ),
                )
            }
            assertEquals(15, storage.count())

            val pruned = storage.prune(RetentionPolicy(maxCount = 8))
            assertEquals(7, pruned)
            assertEquals(8, storage.count())
        }
}
