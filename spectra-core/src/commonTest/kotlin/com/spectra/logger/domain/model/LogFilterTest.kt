package com.spectra.logger.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogFilterTest {
    private val sampleEntry =
        LogEntry(
            id = "test-1",
            timestamp = com.spectra.logger.utils.SpectraTime.now(),
            level = LogLevel.INFO,
            tag = "TestTag",
            message = "Test message",
        )

    @Test
    fun testNoneFilterMatchesAll() {
        assertTrue(LogFilter.NONE.matches(sampleEntry))
    }

    @Test
    fun testLevelFilter() {
        val filter = LogFilter(levels = setOf(LogLevel.INFO, LogLevel.ERROR))
        assertTrue(filter.matches(sampleEntry))

        val errorEntry = sampleEntry.copy(level = LogLevel.ERROR)
        assertTrue(filter.matches(errorEntry))

        val debugEntry = sampleEntry.copy(level = LogLevel.DEBUG)
        assertFalse(filter.matches(debugEntry))
    }

    @Test
    fun testTagFilter() {
        val filter = LogFilter(tags = setOf("TestTag", "OtherTag"))
        assertTrue(filter.matches(sampleEntry))

        val differentTag = sampleEntry.copy(tag = "DifferentTag")
        assertFalse(filter.matches(differentTag))
    }

    @Test
    fun testSearchTextFilter() {
        val filter = LogFilter(searchText = "test")
        assertTrue(filter.matches(sampleEntry))

        val noMatchEntry = sampleEntry.copy(message = "Different message")
        assertFalse(filter.matches(noMatchEntry))
    }

    @Test
    fun testSearchTextCaseInsensitive() {
        val filter = LogFilter(searchText = "TEST")
        assertTrue(filter.matches(sampleEntry))
    }

    @Test
    fun testTimestampFilter() {
        val now = com.spectra.logger.utils.SpectraTime.now()
        val entry = sampleEntry.copy(timestamp = now)

        val beforeFilter = LogFilter(fromTimestamp = now.toEpochMilliseconds() - 1000)
        assertTrue(beforeFilter.matches(entry))

        val afterFilter = LogFilter(fromTimestamp = now.toEpochMilliseconds() + 1000)
        assertFalse(afterFilter.matches(entry))
    }

    @Test
    fun testCombinedFilters() {
        val filter =
            LogFilter(
                levels = setOf(LogLevel.INFO),
                tags = setOf("TestTag"),
                searchText = "message",
            )
        assertTrue(filter.matches(sampleEntry))

        val wrongLevel = sampleEntry.copy(level = LogLevel.DEBUG)
        assertFalse(filter.matches(wrongLevel))
    }

    @Test
    fun testMetadataFilter() {
        val entryWithMetadata = sampleEntry.copy(metadata = mapOf("key1" to "value1", "key2" to "value2"))

        // Filter by key only
        val keyFilter = LogFilter(metadataKey = "key1")
        assertTrue(keyFilter.matches(entryWithMetadata))

        val missingKeyFilter = LogFilter(metadataKey = "missing")
        assertFalse(missingKeyFilter.matches(entryWithMetadata))

        // Filter by key and value
        val keyValueFilter = LogFilter(metadataKey = "key1", metadataValue = "value1")
        assertTrue(keyValueFilter.matches(entryWithMetadata))

        val wrongValueFilter = LogFilter(metadataKey = "key1", metadataValue = "wrong")
        assertFalse(wrongValueFilter.matches(entryWithMetadata))
    }
}
