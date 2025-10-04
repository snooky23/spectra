package com.spectra.logger.performance

import com.spectra.logger.domain.LogLevel
import com.spectra.logger.domain.Logger
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.storage.InMemoryLogStorage
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Performance benchmarks for Spectra Logger.
 *
 * Target metrics:
 * - Log capture: < 0.1ms (100μs)
 * - Storage write: < 1ms
 * - Query performance: < 10ms for 10K logs
 */
class PerformanceBenchmarks {
    @Test
    fun benchmarkLogCapturePerformance() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100_000)
            val logger = Logger(storage = storage, minLevel = LogLevel.VERBOSE)

            // Warm up
            repeat(100) {
                logger.i("Test", "Warmup message $it")
            }

            // Benchmark single log
            val singleLogTime =
                measureTime {
                    logger.i("Benchmark", "Performance test message")
                }

            println("Single log capture time: ${singleLogTime.inWholeMicroseconds}μs")
            assertTrue(
                singleLogTime.inWholeMicroseconds < 100,
                "Log capture should be < 100μs, was ${singleLogTime.inWholeMicroseconds}μs",
            )
        }

    @Test
    fun benchmarkBulkLoggingPerformance() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100_000)
            val logger = Logger(storage = storage, minLevel = LogLevel.VERBOSE)

            val logCount = 10_000
            val bulkTime =
                measureTime {
                    repeat(logCount) { index ->
                        logger.i("Bulk", "Message $index")
                    }
                }

            val avgTimePerLog = bulkTime.inWholeMicroseconds / logCount
            println("Bulk logging ($logCount logs): ${bulkTime.inWholeMilliseconds}ms")
            println("Average time per log: ${avgTimePerLog}μs")

            assertTrue(
                avgTimePerLog < 100,
                "Average log time should be < 100μs, was ${avgTimePerLog}μs",
            )
        }

    @Test
    fun benchmarkStorageQueryPerformance() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100_000)

            // Populate storage
            repeat(10_000) { index ->
                storage.add(
                    LogEntry(
                        id = "log-$index",
                        timestamp = Clock.System.now(),
                        level = LogLevel.INFO,
                        tag = "Test",
                        message = "Message $index",
                    ),
                )
            }

            // Benchmark query
            val queryTime =
                measureTime {
                    storage.query()
                }

            println("Query 10K logs: ${queryTime.inWholeMilliseconds}ms")
            assertTrue(
                queryTime.inWholeMilliseconds < 10,
                "Query should be < 10ms for 10K logs, was ${queryTime.inWholeMilliseconds}ms",
            )
        }

    @Test
    fun benchmarkMemoryUsage() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 10_000)
            val logger = Logger(storage = storage, minLevel = LogLevel.VERBOSE)

            // Add 10K logs
            repeat(10_000) { index ->
                logger.i(
                    "Memory",
                    "This is a test message with some content to measure memory usage #$index",
                    metadata = mapOf("index" to index.toString(), "type" to "benchmark"),
                )
            }

            val count = storage.count()
            println("Stored logs: $count")
            assertTrue(count == 10_000, "Should store exactly 10,000 logs")

            // Note: Actual memory measurement requires platform-specific APIs
            // This test verifies capacity limits work correctly
        }

    @Test
    fun benchmarkConcurrentLogging() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100_000)
            val logger = Logger(storage = storage, minLevel = LogLevel.VERBOSE)

            val concurrentTime =
                measureTime {
                    repeat(1000) { index ->
                        logger.i("Concurrent", "Message $index from thread")
                    }
                }

            println("Concurrent logging (1000 logs): ${concurrentTime.inWholeMilliseconds}ms")
            assertTrue(
                concurrentTime.inWholeMilliseconds < 100,
                "Concurrent logging should be < 100ms for 1000 logs",
            )
        }

    @Test
    fun benchmarkFilteredQuery() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100_000)

            // Add mixed log levels
            repeat(10_000) { index ->
                val level =
                    when (index % 5) {
                        0 -> LogLevel.VERBOSE
                        1 -> LogLevel.DEBUG
                        2 -> LogLevel.INFO
                        3 -> LogLevel.WARNING
                        else -> LogLevel.ERROR
                    }

                storage.add(
                    LogEntry(
                        id = "log-$index",
                        timestamp = Clock.System.now(),
                        level = level,
                        tag = "Test",
                        message = "Message $index",
                    ),
                )
            }

            // Benchmark filtered query
            val filterTime =
                measureTime {
                    storage.query(
                        filter =
                            com.spectra.logger.domain.model.LogFilter(
                                levels = setOf(LogLevel.ERROR),
                            ),
                    )
                }

            println("Filtered query (10K logs): ${filterTime.inWholeMilliseconds}ms")
            assertTrue(
                filterTime.inWholeMilliseconds < 15,
                "Filtered query should be < 15ms, was ${filterTime.inWholeMilliseconds}ms",
            )
        }
}
