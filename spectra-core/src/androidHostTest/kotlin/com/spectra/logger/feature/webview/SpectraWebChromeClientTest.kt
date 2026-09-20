package com.spectra.logger.feature.webview

import android.webkit.ConsoleMessage
import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.logs.model.LogLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpectraWebChromeClientTest {
    @Before
    fun setup() {
        kotlinx.coroutines.runBlocking { SpectraLogger.clear() }
    }

    @After
    fun teardown() {
        SpectraLogger.resetCoroutineScopeForTesting()
        kotlinx.coroutines.runBlocking { SpectraLogger.clear() }
    }

    @Test
    fun `test handleConsoleLog captures log and sends to SpectraLogger`() =
        runTest(UnconfinedTestDispatcher()) {
            SpectraLogger.setCoroutineScopeForTesting(this)

            val client = SpectraWebChromeClient(tag = "CustomWebView")

            client.handleConsoleLog(
                level = LogLevel.INFO,
                message = "Hello from JS console",
                sourceId = "https://example.com/app.js",
                lineNumber = 42,
            )
            advanceUntilIdle()

            val logs = SpectraLogger.query()
            assertEquals(1, logs.size)
            val log = logs.first()

            assertEquals("CustomWebView", log.tag)
            assertEquals("Hello from JS console", log.message)
            assertEquals(LogLevel.INFO, log.level)
            assertEquals("webview", log.metadata["source"])
            assertEquals("https://example.com/app.js", log.metadata["source_url"])
            assertEquals("42", log.metadata["line_number"])
        }

    @Test
    fun `test handleConsoleLog supports isolated injected logger`() {
        var capturedLevel: LogLevel? = null
        var capturedTag: String? = null
        var capturedMsg: String? = null
        var capturedMeta: Map<String, String>? = null

        val client =
            SpectraWebChromeClient(
                tag = "InjectedTag",
                logger = { level, tag, msg, _, meta ->
                    capturedLevel = level
                    capturedTag = tag
                    capturedMsg = msg
                    capturedMeta = meta
                },
            )

        client.handleConsoleLog(
            level = LogLevel.ERROR,
            message = "Uncaught syntax error",
            sourceId = "bundle.js",
            lineNumber = 100,
        )

        assertEquals(LogLevel.ERROR, capturedLevel)
        assertEquals("InjectedTag", capturedTag)
        assertEquals("Uncaught syntax error", capturedMsg)
        assertEquals("bundle.js", capturedMeta?.get("source_url"))
        assertEquals("100", capturedMeta?.get("line_number"))
        assertEquals("webview", capturedMeta?.get("source"))
    }

    @Test
    fun `test mapLevel maps console levels accurately`() {
        val client = SpectraWebChromeClient()

        assertEquals(LogLevel.DEBUG, client.mapLevel(ConsoleMessage.MessageLevel.DEBUG))
        assertEquals(LogLevel.ERROR, client.mapLevel(ConsoleMessage.MessageLevel.ERROR))
        assertEquals(LogLevel.INFO, client.mapLevel(ConsoleMessage.MessageLevel.LOG))
        assertEquals(LogLevel.DEBUG, client.mapLevel(ConsoleMessage.MessageLevel.TIP))
        assertEquals(LogLevel.WARNING, client.mapLevel(ConsoleMessage.MessageLevel.WARNING))
        assertEquals(LogLevel.INFO, client.mapLevel(null))
    }
}
