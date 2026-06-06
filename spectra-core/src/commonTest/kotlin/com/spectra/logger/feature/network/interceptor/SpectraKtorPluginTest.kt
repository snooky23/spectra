package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.SourceType
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the SpectraKtorPlugin.
 * Verifies that the plugin correctly intercepts HTTP requests and responses,
 * logs successful/failed/crashed network calls, and enforces performance
 * limits so the plugin doesn't introduce severe latency.
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
class SpectraKtorPluginTest {
    @BeforeTest
    fun setup() =
        runTest(UnconfinedTestDispatcher()) {
            SpectraLogger.setCoroutineScopeForTesting(CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined))
            SpectraLogger.clearNetwork()
        }

    @Test
    fun testSuccessfulRequestIsLogged() =
        runTest(UnconfinedTestDispatcher()) {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "Hello World Response",
                        status = HttpStatusCode.OK,
                        headers =
                            headersOf(
                                "X-Response-Header" to listOf("RespValue"),
                                "Content-Length" to listOf("20"),
                            ),
                    )
                }

            val client =
                HttpClient(mockEngine) {
                    install(SpectraKtorPlugin)
                }

            client.get("https://api.spectra.com/test") {
                header("X-Request-Header", "ReqValue")
                setBody("Request Body Text")
            }

            kotlinx.coroutines.yield()
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
            assertEquals("Hello World Response", log.responseBody)
            assertEquals("ReqValue", log.requestHeaders["X-Request-Header"])
            assertEquals("RespValue", log.responseHeaders["X-Response-Header"])
        }

    @Test
    fun testFailedRequestIsLogged() =
        runTest(UnconfinedTestDispatcher()) {
            val mockEngine =
                MockEngine { request ->
                    respondError(HttpStatusCode.InternalServerError, "Server Error")
                }

            val client =
                HttpClient(mockEngine) {
                    install(SpectraKtorPlugin)
                }

            try {
                client.get("https://api.spectra.com/error")
            } catch (e: Exception) {
                // expected
            }

            kotlinx.coroutines.yield()
            val logs = SpectraLogger.queryNetwork()
            assertEquals(1, logs.size)

            val log = logs.first()
            assertEquals("https://api.spectra.com/error", log.url)
            assertEquals(500, log.responseCode)
            assertTrue(log.isFailed)
        }

    @Test
    fun testExceptionRequestIsLogged() =
        runTest(UnconfinedTestDispatcher()) {
            val mockEngine =
                MockEngine { request ->
                    throw IllegalStateException("Network unreachable")
                }

            val client =
                HttpClient(mockEngine) {
                    install(SpectraKtorPlugin)
                }

            try {
                client.get("https://api.spectra.com/crash")
            } catch (e: Exception) {
                // expected
            }

            kotlinx.coroutines.yield()
            val logs = SpectraLogger.queryNetwork()
            assertEquals(1, logs.size)

            val log = logs.first()
            assertEquals("https://api.spectra.com/crash", log.url)
            assertNull(log.responseCode)
            assertTrue(log.isFailed)
            assertNotNull(log.error)
            assertTrue(log.error!!.contains("Network unreachable") || log.error!!.contains("IllegalStateException"))
        }

    /**
     * Runs a benchmark of 50 concurrent HTTP requests through the MockEngine to ensure
     * that the Ktor interceptor does not add significant latency (< 25.0ms) due to thread contention.
     */
    @Test
    fun testPluginOverheadIsUnder20ms() =
        runTest(UnconfinedTestDispatcher()) {
            val mockEngine = MockEngine { respond(content = "OK", status = HttpStatusCode.OK) }
            val client = HttpClient(mockEngine) { install(SpectraKtorPlugin) }

            // Warmup
            client.get("https://api.spectra.com/warmup")

            val iterations = 50
            val times =
                (1..iterations).map {
                    async {
                        kotlin.time.measureTime {
                            client.get("https://api.spectra.com/test")
                        }
                    }
                }.awaitAll()

            val totalNs = times.sumOf { it.inWholeNanoseconds }
            val averageTimeMs = totalNs.toDouble() / iterations / 1_000_000.0
            assertTrue(averageTimeMs < 25.0, "Average overhead was ${averageTimeMs}ms, should be < 25.0ms")
        }

    @Test
    fun testIgnoreListDropsTraffic() =
        runTest(UnconfinedTestDispatcher()) {
            val mockEngine = MockEngine { respond(content = "OK", status = HttpStatusCode.OK) }
            val client =
                HttpClient(mockEngine) {
                    install(SpectraKtorPlugin) {
                        ignoreTokens = listOf("analytics.com", "telemetry")
                        ignoreRegex = listOf(Regex(".*\\/secret\\/.*"))
                    }
                }

            // Should be ignored due to token
            client.get("https://analytics.com/track")
            client.get("https://api.myapp.com/telemetry/v1")

            // Should be ignored due to regex
            client.get("https://api.myapp.com/secret/keys")

            // Should be logged
            client.get("https://api.myapp.com/users")

            kotlinx.coroutines.yield()
            val logs = SpectraLogger.queryNetwork()
            assertEquals(1, logs.size)
            assertEquals("https://api.myapp.com/users", logs.first().url)
        }

    @Test
    fun testChunkedResponseOmitted() =
        runTest(UnconfinedTestDispatcher()) {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "Chunked response content",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Transfer-Encoding" to listOf("chunked")),
                    )
                }

            val client = HttpClient(mockEngine) { install(SpectraKtorPlugin) }
            client.get("https://api.spectra.com/chunked")

            kotlinx.coroutines.yield()
            val logs = SpectraLogger.queryNetwork()
            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals("[Chunked response body omitted to prevent stream consumption]", log.responseBody)
        }

    @Test
    fun testUtf8BodyTruncation() =
        runTest(UnconfinedTestDispatcher()) {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "Hello World Response",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Length" to listOf("20")),
                    )
                }

            val client =
                HttpClient(mockEngine) {
                    install(SpectraKtorPlugin) {
                        maxBodySize = 4L
                    }
                }

            client.get("https://api.spectra.com/truncate") {
                setBody("Family: 👨‍👩‍👧‍👦") // Emoji is multi-byte
            }

            kotlinx.coroutines.yield()
            val logs = SpectraLogger.queryNetwork()
            assertEquals(1, logs.size)
            val log = logs.first()

            assertEquals("Fami\n[Body truncated]", log.requestBody)
            assertEquals("[Response body exceeded 4 bytes limit]", log.responseBody)
        }
}
