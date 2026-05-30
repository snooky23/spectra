package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.core.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
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
            sourceType = SourceType.APP
        )
    }

    @Test
    fun testExportLogs() = runTest(UnconfinedTestDispatcher()) {
        val fileSystem = FileSystem(".")
        val storage = FileLogStorage(
            fileSystem = fileSystem,
            maxFileSize = 1000L,
            maxFiles = 3,
            flushThreshold = 2,
            backgroundDispatcher = Dispatchers.Unconfined
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
        
        storage.clear()
    }
}
