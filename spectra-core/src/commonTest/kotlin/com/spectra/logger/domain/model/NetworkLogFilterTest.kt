package com.spectra.logger.domain.model

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkLogFilterTest {
    private val sampleEntry =
        NetworkLogEntry(
            id = "test-1",
            timestamp = Clock.System.now(),
            url = "https://api.example.com/users",
            method = "GET",
            responseCode = 200,
            duration = 150,
        )

    @Test
    fun testNoneFilterMatchesAll() {
        assertTrue(NetworkLogFilter.NONE.matches(sampleEntry))
    }

    @Test
    fun testMethodFilter() {
        val filter = NetworkLogFilter(methods = setOf("GET", "POST"))
        assertTrue(filter.matches(sampleEntry))

        val postEntry = sampleEntry.copy(method = "POST")
        assertTrue(filter.matches(postEntry))

        val deleteEntry = sampleEntry.copy(method = "DELETE")
        assertFalse(filter.matches(deleteEntry))
    }

    @Test
    fun testUrlPatternFilter() {
        val filter = NetworkLogFilter(urlPattern = "api.example")
        assertTrue(filter.matches(sampleEntry))

        val differentUrl = sampleEntry.copy(url = "https://different.com/path")
        assertFalse(filter.matches(differentUrl))
    }

    @Test
    fun testUrlPatternCaseInsensitive() {
        val filter = NetworkLogFilter(urlPattern = "API.EXAMPLE")
        assertTrue(filter.matches(sampleEntry))
    }

    @Test
    fun testStatusCodeFilter() {
        val filter = NetworkLogFilter(statusCodes = setOf(200, 201))
        assertTrue(filter.matches(sampleEntry))

        val notFoundEntry = sampleEntry.copy(responseCode = 404)
        assertFalse(filter.matches(notFoundEntry))
    }

    @Test
    fun testMinDurationFilter() {
        val filter = NetworkLogFilter(minDuration = 100)
        assertTrue(filter.matches(sampleEntry)) // 150ms >= 100ms

        val fastEntry = sampleEntry.copy(duration = 50)
        assertFalse(filter.matches(fastEntry)) // 50ms < 100ms
    }

    @Test
    fun testShowOnlyErrorsFilter() {
        val errorFilter = NetworkLogFilter(showOnlyErrors = true)

        // Failed request (with error)
        val failedEntry = sampleEntry.copy(error = "Network timeout")
        assertTrue(errorFilter.matches(failedEntry))

        // Failed request (no response code)
        val noResponseEntry = sampleEntry.copy(responseCode = null)
        assertTrue(errorFilter.matches(noResponseEntry))

        // Successful request
        assertFalse(errorFilter.matches(sampleEntry))
    }

    @Test
    fun testCombinedFilters() {
        val filter =
            NetworkLogFilter(
                methods = setOf("GET"),
                urlPattern = "example",
                statusCodes = setOf(200),
                minDuration = 100,
            )

        assertTrue(filter.matches(sampleEntry))

        // Wrong method
        val wrongMethod = sampleEntry.copy(method = "POST")
        assertFalse(filter.matches(wrongMethod))

        // Wrong status code
        val wrongStatus = sampleEntry.copy(responseCode = 404)
        assertFalse(filter.matches(wrongStatus))
    }
}
