package com.spectra.logger.feature.crash.ui

import app.cash.turbine.test
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import com.spectra.logger.feature.crash.storage.InMemoryCrashStorage
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CrashViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var crashStorage: InMemoryCrashStorage

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        crashStorage = InMemoryCrashStorage(maxCapacity = 10)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createCrashReport(
        id: String = "crash-1",
        exceptionClass: String = "java.lang.NullPointerException",
        message: String? = "Null reference",
    ) = CrashReport(
        id = id,
        timestamp = SpectraTime.now().toEpochMilliseconds(),
        exceptionClass = exceptionClass,
        message = message,
        stackTrace = "NullPointerException at Example.kt:42",
        threadName = "main",
        severity = CrashSeverity.FATAL,
    )

    @Test
    fun testInitialStateEmpty() =
        runTest(testDispatcher) {
            val viewModel = CrashViewModel(crashStorage = crashStorage)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.crashes.isEmpty())
                assertNull(state.selectedCrash)
            }
        }

    @Test
    fun testObservesRecordedCrashes() =
        runTest(testDispatcher) {
            val viewModel = CrashViewModel(crashStorage = crashStorage)
            advanceUntilIdle()

            val crash1 = createCrashReport(id = "crash-1")
            crashStorage.recordCrash(crash1)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(1, state.crashes.size)
                assertEquals("crash-1", state.crashes.first().id)
            }
        }

    @Test
    fun testSelectCrash() =
        runTest(testDispatcher) {
            val viewModel = CrashViewModel(crashStorage = crashStorage)
            val crash = createCrashReport(id = "crash-selected")
            crashStorage.recordCrash(crash)
            advanceUntilIdle()

            viewModel.selectCrash(crash)

            viewModel.uiState.test {
                val state = awaitItem()
                assertNotNull(state.selectedCrash)
                assertEquals("crash-selected", state.selectedCrash?.id)
            }

            viewModel.selectCrash(null)

            viewModel.uiState.test {
                val state = awaitItem()
                assertNull(state.selectedCrash)
            }
        }

    @Test
    fun testClearCrashes() =
        runTest(testDispatcher) {
            val viewModel = CrashViewModel(crashStorage = crashStorage)
            crashStorage.recordCrash(createCrashReport(id = "c1"))
            crashStorage.recordCrash(createCrashReport(id = "c2"))
            advanceUntilIdle()

            viewModel.selectCrash(createCrashReport(id = "c1"))
            viewModel.clearCrashes()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.crashes.isEmpty())
                assertNull(state.selectedCrash)
            }
        }

    @Test
    fun testSimulateTestCrash() =
        runTest(testDispatcher) {
            val viewModel = CrashViewModel(crashStorage = crashStorage)
            advanceUntilIdle()

            viewModel.simulateTestCrash()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(1, state.crashes.size)
                assertEquals("java.lang.RuntimeException", state.crashes.first().exceptionClass)
                assertEquals(CrashSeverity.NON_FATAL, state.crashes.first().severity)
            }
        }
}
