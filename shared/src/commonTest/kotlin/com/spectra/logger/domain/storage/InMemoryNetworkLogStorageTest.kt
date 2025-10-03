package com.spectra.logger.domain.storage

import app.cash.turbine.test
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.model.NetworkLogFilter
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryNetworkLogStorageTest {
    private fun createEntry(
        id: String,
        method: String = "GET",
        url: String = "https://api.example.com/test",
        responseCode: Int = 200,
    ) = NetworkLogEntry(
        id = id,
        timestamp = Clock.System.now(),
        url = url,
        method = method,
        responseCode = responseCode,
        duration = 100,
    )

    @Test
    fun testAddSingleEntry() =
        runTest {
            val storage = InMemoryNetworkLogStorage(maxCapacity = 10)
            val entry = createEntry("1")

            storage.add(entry)

            assertEquals(1, storage.count())
            val results = storage.query()
            assertEquals(1, results.size)
            assertEquals(entry, results.first())
        }

    @Test
    fun testCircularBufferEviction() =
        runTest {
            val storage = InMemoryNetworkLogStorage(maxCapacity = 3)
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
    fun testQueryWithMethodFilter() =
        runTest {
            val storage = InMemoryNetworkLogStorage()
            storage.add(createEntry("1", method = "GET"))
            storage.add(createEntry("2", method = "POST"))
            storage.add(createEntry("3", method = "GET"))

            val filter = NetworkLogFilter(methods = setOf("POST"))
            val results = storage.query(filter)

            assertEquals(1, results.size)
            assertEquals("2", results.first().id)
        }

    @Test
    fun testQueryWithStatusCodeFilter() =
        runTest {
            val storage = InMemoryNetworkLogStorage()
            storage.add(createEntry("1", responseCode = 200))
            storage.add(createEntry("2", responseCode = 404))
            storage.add(createEntry("3", responseCode = 500))

            val filter = NetworkLogFilter(statusCodes = setOf(404, 500))
            val results = storage.query(filter)

            assertEquals(2, results.size)
            assertEquals("3", results[0].id)
            assertEquals("2", results[1].id)
        }

    @Test
    fun testQueryWithLimit() =
        runTest {
            val storage = InMemoryNetworkLogStorage()
            (1..10).forEach { storage.add(createEntry(it.toString())) }

            val results = storage.query(limit = 3)

            assertEquals(3, results.size)
        }

    @Test
    fun testObserveFlow() =
        runTest {
            val storage = InMemoryNetworkLogStorage()

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
    fun testClear() =
        runTest {
            val storage = InMemoryNetworkLogStorage()
            (1..5).forEach { storage.add(createEntry(it.toString())) }

            assertEquals(5, storage.count())

            storage.clear()

            assertEquals(0, storage.count())
            assertTrue(storage.query().isEmpty())
        }

    @Test
    fun testQueryReturnsNewestFirst() =
        runTest {
            val storage = InMemoryNetworkLogStorage()
            storage.add(createEntry("1"))
            storage.add(createEntry("2"))
            storage.add(createEntry("3"))

            val results = storage.query()

            assertEquals("3", results[0].id)
            assertEquals("2", results[1].id)
            assertEquals("1", results[2].id)
        }
}
