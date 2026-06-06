package com.spectra.logger.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class SourceDetectorTest {
    @Test
    fun testExtractSourceIdFromStack_WebWasm() {
        val wasmStackTrace =
            listOf(
                "at detectSource (http://localhost:8080/spectra-core.wasm:100:20)",
                "at logNetwork (http://localhost:8080/spectra-core.wasm:200:10)",
                "at handleResponse (http://localhost:8080/my-app.wasm:300:5)",
            )
        val source = SourceDetector.extractSourceIdFromStack(wasmStackTrace)
        assertEquals("handleresponse", source, "Should extract the Web-style function name correctly")
    }

    @Test
    fun testExtractSourceIdFromStack_WebJS() {
        val jsStackTrace =
            listOf(
                "detectSource@http://localhost:8080/spectra-core.js:100:20",
                "logNetwork@http://localhost:8080/spectra-core.js:200:10",
                "myFeatureFlow@http://localhost:8080/my-app.js:300:5",
            )
        val source = SourceDetector.extractSourceIdFromStack(jsStackTrace)
        assertEquals("myfeatureflow", source, "Should extract the JS-style function name correctly (lowercased)")
    }
}
