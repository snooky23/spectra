package com.spectra.logger.feature.settings.ui

import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import com.spectra.logger.feature.crash.storage.InMemoryCrashStorage
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.events.storage.InMemoryEventLogStorage
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.logs.storage.InMemoryLogStorage
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.storage.InMemoryNetworkLogStorage
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
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSettingsViewModelCountsAndClearing() =
        runTest(testDispatcher) {
            val logStorage = InMemoryLogStorage(maxCapacity = 100)
            val networkStorage = InMemoryNetworkLogStorage(maxCapacity = 100)
            val eventStorage = InMemoryEventLogStorage(maxCapacity = 100)
            val crashStorage = InMemoryCrashStorage(maxCapacity = 100)

            val viewModel =
                SettingsViewModel(
                    logStorage = logStorage,
                    networkStorage = networkStorage,
                    eventStorage = eventStorage,
                    crashStorage = crashStorage,
                )
            advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.applicationLogCount)
            assertEquals(0, viewModel.uiState.value.networkLogCount)
            assertEquals(0, viewModel.uiState.value.eventLogCount)
            assertEquals(0, viewModel.uiState.value.crashCount)

            // Add logs to storages
            logStorage.add(LogEntry("1", SpectraTime.now(), LogLevel.INFO, "Tag1", "Message 1"))
            networkStorage.add(
                NetworkLogEntry(
                    id = "n1",
                    timestamp = SpectraTime.now(),
                    url = "https://example.com",
                    method = "GET",
                    responseCode = 200,
                ),
            )
            eventStorage.add(
                EventLogEntry(
                    id = "e1",
                    timestamp = SpectraTime.now(),
                    eventType = EventType.SCREEN_VIEW,
                    name = "HomeScreen",
                ),
            )
            crashStorage.recordCrash(
                CrashReport(
                    id = "c1",
                    timestamp = SpectraTime.now().toEpochMilliseconds(),
                    exceptionClass = "NullPointerException",
                    message = "NPE",
                    stackTrace = "at A.b()",
                    threadName = "main",
                    severity = CrashSeverity.FATAL,
                ),
            )
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.applicationLogCount)
            assertEquals(1, viewModel.uiState.value.networkLogCount)
            assertEquals(1, viewModel.uiState.value.eventLogCount)
            assertEquals(1, viewModel.uiState.value.crashCount)

            // Test export bundle content
            val exportText = viewModel.getExportAllText()
            assertTrue(exportText.contains("Spectra Complete Telemetry Debug Report"))
            assertTrue(exportText.contains("Message 1"))
            assertTrue(exportText.contains("https://example.com"))
            assertTrue(exportText.contains("HomeScreen"))

            // Clear storages via ViewModel
            viewModel.clearApplicationLogs()
            viewModel.clearNetworkLogs()
            viewModel.clearEvents()
            viewModel.clearCrashes()
            advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.applicationLogCount)
            assertEquals(0, viewModel.uiState.value.networkLogCount)
            assertEquals(0, viewModel.uiState.value.eventLogCount)
            assertEquals(0, viewModel.uiState.value.crashCount)
        }
}
