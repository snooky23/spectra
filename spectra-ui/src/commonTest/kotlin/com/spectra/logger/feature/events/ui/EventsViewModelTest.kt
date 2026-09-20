package com.spectra.logger.feature.events.ui

import app.cash.turbine.test
import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.events.storage.InMemoryEventLogStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            SpectraLogger.clearEvents()
        }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
    fun testViewModelInitializationAndSearch() =
        runTest(testDispatcher) {
            val viewModel = EventsViewModel()

            viewModel.uiState.test {
                val initial = awaitItem()
                assertTrue(initial.events.isEmpty())

                // Add events to storage
                SpectraLogger.eventStorage.add(createEvent("1", name = "CheckoutScreen", type = EventType.SCREEN_VIEW))
                SpectraLogger.eventStorage.add(createEvent("2", name = "tap_pay", type = EventType.USER_ACTION))

                testDispatcher.scheduler.advanceUntilIdle()

                // Filter by search text
                viewModel.onSearchTextChanged("pay")
                testDispatcher.scheduler.advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals(1, state.filteredEvents.size)
                assertEquals("tap_pay", state.filteredEvents.first().name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testHermeticIsolatedStorageInjection() =
        runTest(testDispatcher) {
            // Hermetic test: verify pure dependency injection with an isolated storage instance
            val isolatedStorage = InMemoryEventLogStorage(maxCapacity = 10)
            isolatedStorage.add(createEvent("iso-1", name = "isolated_event", type = EventType.CUSTOM))

            val viewModel = EventsViewModel(storage = isolatedStorage)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.events.size)
            assertEquals("isolated_event", viewModel.uiState.value.events.first().name)

            // Verify clear through injected storage
            viewModel.clearEvents()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.events.isEmpty())
            assertEquals(0, isolatedStorage.count())
        }

    @Test
    fun testToggleEventType() =
        runTest(testDispatcher) {
            val viewModel = EventsViewModel()
            viewModel.toggleEventType(EventType.SCREEN_VIEW)

            val state = viewModel.uiState.value
            assertTrue(state.selectedEventTypes.contains(EventType.SCREEN_VIEW))

            viewModel.toggleEventType(EventType.SCREEN_VIEW)
            assertTrue(viewModel.uiState.value.selectedEventTypes.isEmpty())
        }
}
