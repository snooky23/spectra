package com.spectra.logger.feature.events.storage

import app.cash.turbine.test
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryEventLogStorageTest {
    private fun createEvent(
        id: String,
        name: String = "TestEvent",
        type: EventType = EventType.USER_ACTION,
        durationMs: Long? = null,
        parameters: Map<String, String> = emptyMap(),
    ) = EventLogEntry(
        id = id,
        timestamp = SpectraTime.now(),
        eventType = type,
        name = name,
        parameters = parameters,
        durationMs = durationMs,
    )

    @Test
    fun testAddSingleEvent() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 10)
            val event = createEvent("1", name = "button_click")

            storage.add(event)

            assertEquals(1, storage.count())
            val results = storage.query()
            assertEquals(1, results.size)
            assertEquals(event, results.first())
        }

    @Test
    fun testAddMultipleEvents() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 10)
            val events = (1..5).map { createEvent(it.toString(), name = "event_$it") }

            storage.addAll(events)

            assertEquals(5, storage.count())
            val results = storage.query()
            assertEquals(5, results.size)
        }

    @Test
    fun testBufferEviction() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 3)
            val events = (1..5).map { createEvent(it.toString(), name = "event_$it") }

            events.forEach { storage.add(it) }

            assertEquals(3, storage.count())
            val results = storage.query()
            assertEquals(listOf("5", "4", "3"), results.map { it.id })
        }

    @Test
    fun testQueryWithFilter() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 10)
            storage.add(createEvent("1", name = "Home", type = EventType.SCREEN_VIEW, durationMs = 120))
            storage.add(createEvent("2", name = "buy_tap", type = EventType.USER_ACTION))
            storage.add(createEvent("3", name = "Profile", type = EventType.SCREEN_VIEW, durationMs = 350))

            val screenViews = storage.query(EventFilter(eventTypes = setOf(EventType.SCREEN_VIEW)))
            assertEquals(2, screenViews.size)

            val durationFiltered = storage.query(EventFilter(minDurationMs = 200))
            assertEquals(1, durationFiltered.size)
            assertEquals("Profile", durationFiltered.first().name)

            val textFiltered = storage.query(EventFilter(searchText = "buy"))
            assertEquals(1, textFiltered.size)
            assertEquals("buy_tap", textFiltered.first().name)
        }

    @Test
    fun testObserveFlow() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 10)
            val event = createEvent("1", name = "flow_test", type = EventType.CUSTOM)

            storage.observe().test {
                storage.add(event)
                val emitted = awaitItem()
                assertEquals("1", emitted.id)
                assertEquals("flow_test", emitted.name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testClear() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 10)
            storage.add(createEvent("1"))
            storage.add(createEvent("2"))

            assertEquals(2, storage.count())
            storage.clear()
            assertEquals(0, storage.count())
            assertTrue(storage.query().isEmpty())
        }
}
