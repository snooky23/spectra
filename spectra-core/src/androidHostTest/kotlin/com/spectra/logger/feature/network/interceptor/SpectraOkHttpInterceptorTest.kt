package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.SourceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpectraOkHttpInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(SpectraOkHttpInterceptor(maxBodySize = 250_000L))
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        SpectraLogger.resetCoroutineScopeForTesting()
        kotlinx.coroutines.runBlocking { SpectraLogger.clearNetwork() }
    }

    @Test
    fun `test successful 200 OK request logs correctly`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("Success Response Body")
                .addHeader("Content-Type", "text/plain")
                .addHeader("X-Response-Test", "ResponseValue")
        )

        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .addHeader("X-Request-Test", "RequestValue")
            .post("Request Body Payload".toRequestBody("text/plain".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute()
        advanceUntilIdle()

        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        val log = logs.first()

        assertTrue(log.url.contains("/api/test"))
        assertEquals("POST", log.method)
        assertEquals(200, log.responseCode)
        assertTrue(log.isSuccessful)
        assertNull(log.error)
        assertEquals("okhttp", log.source)
        assertEquals(SourceType.PLUGIN, log.sourceType)

        assertEquals("Request Body Payload", log.requestBody)
        assertEquals("Success Response Body", log.responseBody)
        assertEquals("RequestValue", log.requestHeaders["X-Request-Test"])
        assertEquals("ResponseValue", log.responseHeaders["X-Response-Test"])
    }

    @Test
    fun `test 201 Created request logs correctly`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody("Created"))

        val request = Request.Builder().url(mockWebServer.url("/api/create")).build()
        okHttpClient.newCall(request).execute()
        advanceUntilIdle()

        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        assertEquals(201, logs.first().responseCode)
        assertTrue(logs.first().isSuccessful)
    }

    @Test
    fun `test 400 Bad Request logs correctly`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        val request = Request.Builder().url(mockWebServer.url("/api/bad")).build()
        okHttpClient.newCall(request).execute()
        advanceUntilIdle()

        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        assertEquals(400, logs.first().responseCode)
        assertTrue(logs.first().isFailed) // 400 is not successful
    }

    @Test
    fun `test 404 Not Found logs correctly`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val request = Request.Builder().url(mockWebServer.url("/api/missing")).build()
        okHttpClient.newCall(request).execute()
        advanceUntilIdle()

        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        assertEquals(404, logs.first().responseCode)
        assertTrue(logs.first().isFailed)
    }

    @Test
    fun `test 500 Server Error request logs correctly`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Crash"))

        val request = Request.Builder().url(mockWebServer.url("/api/error")).build()
        okHttpClient.newCall(request).execute()
        advanceUntilIdle()

        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        assertEquals(500, logs.first().responseCode)
        assertTrue(logs.first().isFailed)
    }

    @Test
    fun `test network exception is logged correctly`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        // Create a client that will fail to resolve host
        val failingClient = OkHttpClient.Builder()
            .addInterceptor(SpectraOkHttpInterceptor())
            .build()

        val request = Request.Builder()
            .url("https://invalid.domain.that.does.not.exist.spectra")
            .build()

        try {
            failingClient.newCall(request).execute()
        } catch (e: Exception) {
            // expected UnknownHostException
        }
        advanceUntilIdle()

        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        val log = logs.first()
        
        assertNull(log.responseCode)
        assertTrue(log.isFailed)
        assertNotNull(log.error)
        assertTrue(log.error!!.contains("UnknownHostException") || log.error!!.contains("java.net"))
    }
    
    @Test
    fun `test plugin overhead is under 5ms`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))
        
        // Warmup
        okHttpClient.newCall(Request.Builder().url(mockWebServer.url("/warmup")).build()).execute()
        
        val iterations = 50
        val totalTime = kotlin.system.measureTimeMillis {
            repeat(iterations) {
                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))
                okHttpClient.newCall(Request.Builder().url(mockWebServer.url("/benchmark")).build()).execute()
            }
        }
        advanceUntilIdle()
        
        val averageTimeMs = totalTime.toDouble() / iterations
        assertTrue(averageTimeMs < 5.0, "Average overhead was ${averageTimeMs}ms, should be < 5.0ms")
    }

    @Test
    fun `test ignore list successfully drops traffic without logging`() = runTest(UnconfinedTestDispatcher()) {
        SpectraLogger.setCoroutineScopeForTesting(this)
        SpectraLogger.clearNetwork()
        
        val ignoreClient = OkHttpClient.Builder()
            .addInterceptor(SpectraOkHttpInterceptor(
                ignoreTokens = listOf("analytics.com", "telemetry"),
                ignoreRegex = listOf(Regex(".*\\/secret\\/.*"))
            ))
            .build()
            
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))
        
        // Should be ignored due to token
        ignoreClient.newCall(Request.Builder().url(mockWebServer.url("/track?host=analytics.com")).build()).execute()
        ignoreClient.newCall(Request.Builder().url(mockWebServer.url("/telemetry/v1")).build()).execute()
        
        // Should be ignored due to regex
        ignoreClient.newCall(Request.Builder().url(mockWebServer.url("/secret/keys")).build()).execute()
        
        // Should be logged
        ignoreClient.newCall(Request.Builder().url(mockWebServer.url("/users")).build()).execute()
        
        advanceUntilIdle()
        
        val logs = SpectraLogger.queryNetwork()
        assertEquals(1, logs.size)
        assertTrue(logs.first().url.contains("/users"))
    }
}
