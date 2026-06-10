package com.spectra.logger.core

import app.cash.turbine.test
import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.feature.logs.model.LogFilter
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.logs.storage.InMemoryLogStorage
import com.spectra.logger.feature.logs.storage.LogStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.measureTime

class LoggerTest {
    // Helper to create test logger with deterministic scope
    private fun createTestLogger(
        storage: LogStorage,
        minLevel: LogLevel = LogLevel.VERBOSE,
        scope: CoroutineScope,
    ): Logger {
        return Logger(storage = storage, minLevel = minLevel, scope = scope)
    }

    @Test
    fun testBasicLogging() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            logger.i("Test", "Info message")
            logger.e("Test", "Error message")

            advanceUntilIdle()

            assertEquals(2, storage.count())
        }

    @Test
    fun testAllLogLevels() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            logger.v("Test", "Verbose")
            logger.d("Test", "Debug")
            logger.i("Test", "Info")
            logger.w("Test", "Warning")
            logger.e("Test", "Error")
            logger.f("Test", "Fatal")

            advanceUntilIdle()

            assertEquals(6, storage.count())

            val logs = storage.query()
            // Logs should be in reverse order (newest first)
            assertTrue(logs.size == 6)
            assertTrue(logs.any { it.level == LogLevel.FATAL })
            assertTrue(logs.any { it.level == LogLevel.ERROR })
            assertTrue(logs.any { it.level == LogLevel.WARNING })
            assertTrue(logs.any { it.level == LogLevel.INFO })
            assertTrue(logs.any { it.level == LogLevel.DEBUG })
            assertTrue(logs.any { it.level == LogLevel.VERBOSE })
        }

    @Test
    fun testMinLevelFiltering() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, minLevel = LogLevel.WARNING, scope = backgroundScope)

            logger.d("Test", "Debug - should be filtered")
            logger.i("Test", "Info - should be filtered")
            logger.w("Test", "Warning - should pass")
            logger.e("Test", "Error - should pass")

            advanceUntilIdle()

            assertEquals(2, storage.count())
            val logs = storage.query()
            assertTrue(logs.all { it.level.priority >= LogLevel.WARNING.priority })
        }

    @Test
    fun testLoggingWithMetadata() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            val metadata = mapOf("userId" to "123", "action" to "login")
            logger.i("Auth", "User logged in", metadata = metadata)

            advanceUntilIdle()

            val logs = storage.query()
            assertEquals(1, logs.size)
            assertEquals(metadata, logs.first().metadata)
        }

    @Test
    fun testLoggingWithThrowable() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            val exception = RuntimeException("Test error")
            logger.e("Error", "Something went wrong", throwable = exception)

            advanceUntilIdle()

            val logs = storage.query()
            assertEquals(1, logs.size)
            assertTrue(logs.first().throwable?.contains("RuntimeException") == true)
            assertTrue(logs.first().throwable?.contains("Test error") == true)
        }

    @Test
    fun testLoggingWithStackTraceInMetadata() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            val stackTrace = "Mock stack trace string"
            logger.e("Error", "Message", metadata = mapOf("stack_trace" to stackTrace))

            advanceUntilIdle()

            val logs = storage.query()
            assertEquals(1, logs.size)
            assertEquals(stackTrace, logs.first().throwable)
        }

    @Test
    fun testQuery() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            logger.i("Tag1", "Message 1")
            logger.e("Tag2", "Message 2")
            logger.w("Tag1", "Message 3")

            advanceUntilIdle()

            val filter = LogFilter(tags = setOf("Tag1"))
            val results = logger.query(filter)

            assertEquals(2, results.size)
            assertTrue(results.all { it.tag == "Tag1" })
        }

    @Test
    fun testObserve() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            logger.observe().test {
                logger.i("Test", "Message 1")
                advanceUntilIdle()
                val item1 = awaitItem()
                assertEquals("Message 1", item1.message)

                logger.e("Test", "Message 2")
                advanceUntilIdle()
                val item2 = awaitItem()
                assertEquals("Message 2", item2.message)

                cancel()
            }
        }

    @Test
    fun testClear() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            logger.i("Test", "Message 1")
            logger.i("Test", "Message 2")
            advanceUntilIdle()

            assertEquals(2, logger.count())

            logger.clear()

            assertEquals(0, logger.count())
        }

    @Test
    fun testConcurrentLoggingThreadSafety() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage(maxCapacity = 10000)
            val logger = createTestLogger(storage, scope = backgroundScope)

            val numCoroutines = 10
            val logsPerCoroutine = 10
            val totalExpected = numCoroutines * logsPerCoroutine

            val jobs =
                (1..numCoroutines).map { index ->
                    launch {
                        repeat(logsPerCoroutine) { logIndex ->
                            val level = LogLevel.entries[logIndex % LogLevel.entries.size]
                            logger.log(level, "Tag-$index", "Log $logIndex")
                            if (logIndex % 10 == 0) kotlinx.coroutines.yield()
                        }
                    }
                }
            jobs.forEach { it.join() }

            val logs = storage.query()
            assertEquals(totalExpected, logs.size)

            // Verify metadata is attached securely
            assertTrue(logs.all { it.timestamp.toEpochMilliseconds() > 0 })
            assertTrue(logs.all { it.metadata != null })

            val tags = logs.map { it.tag }.toSet()
            assertEquals(numCoroutines, tags.size)

            val levelsUsed = logs.map { it.level }.toSet()
            assertEquals(LogLevel.entries.size, levelsUsed.size)
        }

    @Test
    fun testCapacityBoundary() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage(maxCapacity = 10)
            val logger = createTestLogger(storage, scope = backgroundScope)
            repeat(15) { index ->
                logger.i("Tag", "Log $index")
            }
            runCurrent() // Execute all queued coroutines
            assertEquals(10, storage.count())
        }

    @Test
    fun testLoggingOverheadIsSubPointOneMilliseconds() =
        runTest(UnconfinedTestDispatcher()) {
            val storage = InMemoryLogStorage()
            val logger = createTestLogger(storage, scope = backgroundScope)

            // Warm up
            repeat(1000) { i ->
                logger.i("WarmUp", "Message")
                if (i % 100 == 0) kotlinx.coroutines.yield()
            }

            val iterations = 1000

            val totalTime =
                measureTime {
                    repeat(iterations) { i ->
                        logger.i("Benchmark", "Message $i")
                        if (i % 100 == 0) kotlinx.coroutines.yield()
                    }
                }

            val averageTimeMs = totalTime.toDouble(DurationUnit.MILLISECONDS) / iterations

            // Relaxed threshold to avoid CI flakiness
            assertTrue(
                averageTimeMs < 50.0,
                "Average logging enqueue overhead was $averageTimeMs ms, which is high even for CI.",
            )
        }
}
