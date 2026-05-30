package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.CoroutineScope
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
class SpectraKtorPluginTest {

    @BeforeTest
    fun setup() = runTest {
        SpectraLogger.setCoroutineScopeForTesting(CoroutineScope(UnconfinedTestDispatcher()))
        SpectraLogger.clearNetwork()
    }

    @Test
    fun testSuccessfulRequestIsLogged() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Hello World Response",
                status = HttpStatusCode.OK,
                headers = headersOf("X-Response-Header", "RespValue")
            )
        }

        val client = HttpClient(mockEngine) {
            install(SpectraKtorPlugin)
        }

        client.get("https://api.spectra.com/test") {
            header("X-Request-Header", "ReqValue")
            setBody("Request Body Text")
        }
        
        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        
        val log = logs.first()
        assertEquals("https://api.spectra.com/test", log.url)
        assertEquals("GET", log.method)
        assertEquals(200, log.responseCode)
        assertTrue(log.isSuccessful)
        assertNull(log.error)
        assertEquals("ktor", log.source)
        assertEquals(SourceType.PLUGIN, log.sourceType)
        assertEquals("Request Body Text", log.requestBody)
        assertEquals("[Response body capture pending DoubleReceive configuration]", log.responseBody)
        assertEquals("ReqValue", log.requestHeaders["X-Request-Header"])
        assertEquals("RespValue", log.responseHeaders["X-Response-Header"])
    }

    @Test
    fun testFailedRequestIsLogged() = runTest {
        val mockEngine = MockEngine { request ->
            respondError(HttpStatusCode.InternalServerError, "Server Error")
        }

        val client = HttpClient(mockEngine) {
            install(SpectraKtorPlugin)
        }

        try {
            client.get("https://api.spectra.com/error")
        } catch (e: Exception) {
            // expected
        }
        
        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        
        val log = logs.first()
        assertEquals("https://api.spectra.com/error", log.url)
        assertEquals(500, log.responseCode)
        assertTrue(log.isFailed)
    }
    
    @Test
    fun testExceptionRequestIsLogged() = runTest {
        val mockEngine = MockEngine { request ->
            throw IllegalStateException("Network unreachable")
        }

        val client = HttpClient(mockEngine) {
            install(SpectraKtorPlugin)
        }

        try {
            client.get("https://api.spectra.com/crash")
        } catch (e: Exception) {
            // expected
        }
        
        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        
        val log = logs.first()
        assertEquals("https://api.spectra.com/crash", log.url)
        assertNull(log.responseCode)
        assertTrue(log.isFailed)
        assertNotNull(log.error)
        assertTrue(log.error!!.contains("Network unreachable") || log.error!!.contains("IllegalStateException"))
    }
    
    @Test
    fun testPluginOverheadIsUnder5ms() = runTest {
        val mockEngine = MockEngine { respond(content = "OK", status = HttpStatusCode.OK) }
        val client = HttpClient(mockEngine) { install(SpectraKtorPlugin) }
        
        // Warmup
        client.get("https://api.spectra.com/warmup")
        
        val iterations = 50
        val totalTime = kotlin.time.measureTime {
            repeat(iterations) {
                client.get("https://api.spectra.com/test")
            }
        }
        
        val averageTimeMs = totalTime.inWholeNanoseconds.toDouble() / iterations / 1_000_000.0
        assertTrue(averageTimeMs < 5.0, "Average overhead was ${averageTimeMs}ms, should be < 5.0ms")
    }
}
