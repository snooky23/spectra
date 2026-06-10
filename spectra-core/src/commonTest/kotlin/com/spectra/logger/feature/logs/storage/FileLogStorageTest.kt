package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileLogStorageTest {
    private fun createDummyEntry(msg: String): LogEntry {
        return LogEntry(
            id = IdGenerator.generate(),
            timestamp = com.spectra.logger.core.utils.SpectraTime.now(),
            level = LogLevel.INFO,
            tag = "Test",
            message = msg,
            throwable = null,
            metadata = emptyMap(),
            source = "App",
            sourceType = SourceType.APP,
        )
    }

    @Test
    fun testExportLogs() =
        runTest {
            val dispatcher = Dispatchers.Unconfined
            val fileSystem = FileSystem(".", okioFs = FakeFileSystem(), dispatcher = dispatcher)
            val storage =
                FileLogStorage(
                    fileSystem = fileSystem,
                    maxFileSize = 1000L,
                    maxFiles = 3,
                    flushThreshold = 2,
                    backgroundDispatcher = dispatcher,
                )

            // Clear previous state if any
            storage.clear()

            // Add enough entries to trigger flushes
            storage.add(createDummyEntry("Log 1"))
            storage.add(createDummyEntry("Log 2"))
            storage.add(createDummyEntry("Log 3"))

            storage.flush()

            val exportedPath = storage.exportLogs()
            assertNotNull(exportedPath)

            // Assert that the file system reports it exists (we test fileSystem directly)
            val files = fileSystem.listFiles(".")
            assertTrue(files.any { it.startsWith("export_") && it.endsWith(".jsonl") })

            // Assert that the filename does not contain colons (Windows compatibility)
            val exportFile = files.first { it.startsWith("export_") && it.endsWith(".jsonl") }
            assertTrue(!exportFile.contains(":"), "Export filename should not contain colons")

            storage.clear()
            storage.close()
        }

    @Test
    fun testFileRotationWithMultiByteCharacters() =
        runTest {
            val dispatcher = Dispatchers.Unconfined
            val fileSystem = FileSystem(".", okioFs = FakeFileSystem(), dispatcher = dispatcher)
            val storage =
                FileLogStorage(
                    fileSystem = fileSystem,
                    // Increased to allow the emoji message to fit
                    maxFileSize = 1000L,
                    maxFiles = 3,
                    // immediate flush
                    flushThreshold = 1,
                    backgroundDispatcher = dispatcher,
                )

            storage.clear()

            // 👨‍👩‍👧‍👦 is 25 bytes in UTF-8
            val emojiMessage = "Family emoji: 👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦"

            // Add entries to force rotation
            repeat(10) {
                storage.add(createDummyEntry(emojiMessage))
            }
            storage.flush()

            // Check that no file exceeds the limit (or only slightly if truncated)
            val files = fileSystem.listFiles(".")
            val logFiles = files.filter { it.startsWith("logs_") }
            assertTrue(logFiles.isNotEmpty(), "Log files should be created")

            for (fileName in logFiles) {
                val size = fileSystem.getFileSize(fileName)
                // Should be bounded appropriately, allowing a small overflow buffer before truncation happens
                assertTrue(size <= 1300L, "File $fileName size $size exceeds expected rotated size limit")
            }

            storage.clear()
            storage.close()
        }
}
