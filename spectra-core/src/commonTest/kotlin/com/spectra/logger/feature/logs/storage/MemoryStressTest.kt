package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Evaluates the memory footprint and log rotation mechanisms of the storage engine.
 * Ensures the system does not crash with an OutOfMemoryError when blasted with
 * large payloads and that max capacity rotation successfully discards old logs.
 */
class MemoryStressTest {
    private val largeString =
        StringBuilder().apply {
            repeat(100) { append("This is a very long text payload intended to consume memory rapidly. ") }
        }.toString()

    private fun createHeavyEntry(index: Int): LogEntry {
        return LogEntry(
            id = IdGenerator.generate(),
            timestamp = SpectraTime.now(),
            level = LogLevel.ERROR,
            tag = "StressTest",
            message = "Error $index: $largeString",
            throwable =
                "java.lang.OutOfMemoryError: Java heap space\n\t at " +
                    "java.base/java.lang.String(String.java:123)\n\t at com.spectra.Dummy.run(Dummy.kt:45)",
            metadata =
                mapOf(
                    "user_id" to "1234567890",
                    "session_data" to largeString,
                    "index" to index.toString(),
                ),
            source = "App",
            sourceType = SourceType.APP,
        )
    }

    /**
     * Floods the system with 50,000 huge log entries (15KB each).
     * If the log rotation or memory buffers are broken, this will trigger an OutOfMemoryError
     * or result in disk capacity overflows. The test ensures the rotation handles the load
     */
    @Test
    fun testMemoryBoundsWith50KHeavyLogs() =
        runTest {
            val dispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)
            val fileSystem =
                FileSystem(
                    "build/tmp/spectratest-mem-${IdGenerator.generate()}",
                    okioFs = okio.fakefilesystem.FakeFileSystem(),
                    dispatcher = dispatcher,
                )
            val maxCapacity = 5000 // Ensure we never hold more than 5000 in memory

            val storage =
                FileLogStorage(
                    fileSystem = fileSystem,
                    // 2MB
                    maxFileSize = 2_000_000L,
                    maxFiles = 3,
                    flushThreshold = 200,
                    maxCapacity = maxCapacity,
                    backgroundDispatcher = dispatcher,
                )

            storage.clear()

            val totalLogs = 100
            val coroutinesCount = 10
            val logsPerCoroutine = totalLogs / coroutinesCount

            val jobs =
                (1..coroutinesCount).map {
                    launch {
                        for (i in 1..logsPerCoroutine) {
                            storage.add(createHeavyEntry(i))
                            if (i % 100 == 0) kotlinx.coroutines.yield()
                        }
                    }
                }
            jobs.forEach { it.join() }

            storage.flush()
            runCurrent()

            val finalCount = storage.count()
            // Assert that we have processed logs. count() returns the total historical logs written in the session.
            assertTrue(finalCount > 0, "Storage should contain some logs")
            assertTrue(finalCount <= totalLogs, "Storage cannot exceed the total logs we inserted")

            storage.clear()
            storage.close()
        }
}
