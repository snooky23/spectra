package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Tests the system's ability to handle high throughput logging (10K+ logs).
 * It verifies that batching works efficiently, background flushes don't block
 * the main thread, and that overall performance overhead is within limits.
 */
class LoadTest {
    /**
     * Creates a dummy log entry for testing.
     */
    private fun createDummyEntry(index: Int): LogEntry {
        return LogEntry(
            id = IdGenerator.generate(),
            timestamp = SpectraTime.now(),
            level = LogLevel.INFO,
            tag = "LoadTest",
            message = "This is load test message #$index",
            throwable = null,
            metadata = emptyMap(),
            source = "App",
            sourceType = SourceType.APP,
        )
    }

    /**
     * Spawns 100 coroutines to concurrently write a total of 10,000 logs.
     * Validates that the total execution time (overhead) per log is minimal (< 5.0ms on average)
     * and that no logs are lost under high concurrency.
     */
    @Test
    fun test10KLogsPerformance() =
        runTest {
            val dispatcher = Dispatchers.Unconfined
            val fileSystem =
                FileSystem(
                    "build/tmp/spectratest-load-${IdGenerator.generate()}",
                    okioFs = okio.fakefilesystem.FakeFileSystem(),
                    dispatcher = dispatcher,
                )
            val storage =
                FileLogStorage(
                    fileSystem = fileSystem,
                    maxFileSize = 10_000_000L, // 10MB
                    maxFiles = 5,
                    flushThreshold = 100,
                    backgroundDispatcher = dispatcher,
                )

            // Clear previous state if any
            storage.clear()

            val totalLogs = 10_000
            val coroutinesCount = 100
            val logsPerCoroutine = totalLogs / coroutinesCount

            val addTime =
                measureTime {
                    withContext(Dispatchers.Default) {
                        val jobs =
                            (1..coroutinesCount).map {
                                launch {
                                    for (i in 1..logsPerCoroutine) {
                                        storage.add(createDummyEntry(i))
                                    }
                                }
                            }
                        jobs.forEach { it.join() }
                    }
                }

            // Explicitly flush pending items
            storage.flush()

            // Wait for background disk I/O to catch up (max 10 seconds wait in tests to avoid CI timeouts)
            var retries = 0
            while (storage.count() < totalLogs && retries < 100) {
                withContext(Dispatchers.Default) { delay(100) }
                retries++
            }

            assertEquals(totalLogs, storage.count())

            val averageTimeMs = addTime.inWholeNanoseconds.toDouble() / totalLogs / 1_000_000.0

            // Should be very fast. Adjusted from 1ms to 5ms to avoid GitHub Actions shared CPU flakiness.
            assertTrue(
                averageTimeMs < 5.0,
                "Average log overhead was $averageTimeMs ms, which exceeds the 5.0ms performance boundary.",
            )

            storage.clear()
            storage.close()
        }
}
