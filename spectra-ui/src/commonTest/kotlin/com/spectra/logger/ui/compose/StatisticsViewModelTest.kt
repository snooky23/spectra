package com.spectra.logger.ui.compose

import app.cash.turbine.test
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            SpectraLogger.clear()
        }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
    fun testViewModelInitializationAndObservation() =
        runTest(testDispatcher) {
            val viewModel = StatisticsViewModel()

            viewModel.uiState.test {
                // Initial state from StateFlow
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)
                assertEquals(0, initialState.statistics.totalLogs)

                // State after repository initialization (might be instantaneous or require yielding)
                val loadedState = awaitItem()
                assertFalse(loadedState.isLoading)
                assertEquals(0, loadedState.statistics.totalLogs)

                // Add a log to the global storage
                SpectraLogger.logStorage.add(createEntry("1", 1000L, LogLevel.ERROR))

                // Wait for the ViewModel to pick up the change
                val updatedState = awaitItem()
                assertFalse(updatedState.isLoading)
                assertEquals(1, updatedState.statistics.totalLogs)
                assertEquals(1, updatedState.statistics.levelCounts[LogLevel.ERROR])

                cancelAndIgnoreRemainingEvents()
            }
        }
}
