package com.spectra.logger.domain.storage

import app.cash.turbine.test
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.LogLevel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryLogStorageTest {
    private fun createEntry(
        id: String,
        level: LogLevel = LogLevel.INFO,
        tag: String = "Test",
    ) = LogEntry(
        id = id,
        timestamp = Clock.System.now(),
        level = level,
        tag = tag,
        message = "Message $id",
    )

    @Test
    fun testAddSingleEntry() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 10)
            val entry = createEntry("1")

            storage.add(entry)

            assertEquals(1, storage.count())
            val results = storage.query()
            assertEquals(1, results.size)
            assertEquals(entry, results.first())
        }

    @Test
    fun testAddMultipleEntries() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 10)
            val entries = (1..5).map { createEntry(it.toString()) }

            storage.addAll(entries)

            assertEquals(5, storage.count())
        }

    @Test
    fun testCircularBufferEviction() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 3)
            val entries = (1..5).map { createEntry(it.toString()) }

            entries.forEach { storage.add(it) }

            assertEquals(3, storage.count())
            val results = storage.query()
            assertEquals(3, results.size)
            // Should have entries 3, 4, 5 (newest first)
            assertEquals("5", results[0].id)
            assertEquals("4", results[1].id)
            assertEquals("3", results[2].id)
        }

    @Test
    fun testQueryWithFilter() =
        runTest {
            val storage = InMemoryLogStorage()
            storage.add(createEntry("1", LogLevel.DEBUG))
            storage.add(createEntry("2", LogLevel.INFO))
            storage.add(createEntry("3", LogLevel.ERROR))

            val filter = LogFilter(levels = setOf(LogLevel.ERROR))
            val results = storage.query(filter)

            assertEquals(1, results.size)
            assertEquals("3", results.first().id)
        }

    @Test
    fun testQueryWithLimit() =
        runTest {
            val storage = InMemoryLogStorage()
            (1..10).forEach { storage.add(createEntry(it.toString())) }

            val results = storage.query(limit = 3)

            assertEquals(3, results.size)
        }

    @Test
    fun testObserveFlow() =
        runTest {
            val storage = InMemoryLogStorage()

            storage.observe().test {
                storage.add(createEntry("1"))
                val item = awaitItem()
                assertEquals("1", item.id)

                storage.add(createEntry("2"))
                val item2 = awaitItem()
                assertEquals("2", item2.id)

                cancel()
            }
        }

    @Test
    fun testObserveWithFilter() =
        runTest {
            val storage = InMemoryLogStorage()
            val filter = LogFilter(levels = setOf(LogLevel.ERROR))

            storage.observe(filter).test {
                storage.add(createEntry("1", LogLevel.DEBUG))
                storage.add(createEntry("2", LogLevel.ERROR))

                val item = awaitItem()
                assertEquals("2", item.id)
                assertEquals(LogLevel.ERROR, item.level)

                cancel()
            }
        }

    @Test
    fun testClear() =
        runTest {
            val storage = InMemoryLogStorage()
            (1..5).forEach { storage.add(createEntry(it.toString())) }

            assertEquals(5, storage.count())

            storage.clear()

            assertEquals(0, storage.count())
            assertTrue(storage.query().isEmpty())
        }

    @Test
    fun testQueryReturnsNewestFirst() =
        runTest {
            val storage = InMemoryLogStorage()
            storage.add(createEntry("1"))
            storage.add(createEntry("2"))
            storage.add(createEntry("3"))

            val results = storage.query()

            assertEquals("3", results[0].id)
            assertEquals("2", results[1].id)
            assertEquals("1", results[2].id)
        }
}
