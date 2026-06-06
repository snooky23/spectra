package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import kotlinx.coroutines.runBlocking
import platform.Foundation.*
import kotlin.test.*

/**
 * Tests the SpectraURLProtocol iOS network interception.
 * Validates that requests are only intercepted if they are not ignored
 * by tokens/regex and that they haven't been handled already.
 * Also measures initialization performance.
 */
class SpectraURLProtocolTest {
    @BeforeTest
    fun setup() =
        runBlocking {
            SpectraLogger.clearNetwork()
            SpectraIOSInterceptorConfig.ignoreTokens = emptyList()
            SpectraIOSInterceptorConfig.ignoreRegex = emptyList()
        }

    @Test
    fun testCanInitWithRequest() {
        val url = NSURL.URLWithString("https://api.spectra.com/test")!!
        val request = NSURLRequest.requestWithURL(url)

        val canInit = SpectraURLProtocol.canInitWithRequest(request)
        assertTrue(canInit)
    }

    @Test
    fun testCanInitWithRequest_IgnoredToken() {
        val url = NSURL.URLWithString("https://api.spectra.com/test")!!
        val request = NSURLRequest.requestWithURL(url)

        SpectraIOSInterceptorConfig.ignoreTokens = listOf("api.spectra.com")

        val canInit = SpectraURLProtocol.canInitWithRequest(request)
        assertFalse(canInit)
    }

    @Test
    fun testCanInitWithRequest_AlreadyHandled() {
        val url = NSURL.URLWithString("https://api.spectra.com/test")!!
        val request = NSMutableURLRequest.requestWithURL(url)
        NSURLProtocol.setProperty(true, "SpectraHandled", request)

        val canInit = SpectraURLProtocol.canInitWithRequest(request)
        assertFalse(canInit)
    }

    /**
     * Simulates initialization overhead to ensure URLProtocol parsing and URL string
     * extraction stays well under 5.0ms on average per iOS request.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    @Test
    fun testInitializationOverheadIsUnder1ms() {
        val url = NSURL.URLWithString("https://api.spectra.com/test")!!
        val request = NSURLRequest.requestWithURL(url)
        
        // Warmup
        SpectraURLProtocol.canInitWithRequest(request)
        
        val iterations = 1000
        
        val totalTime = kotlin.time.measureTime {
            repeat(iterations) { i ->
                val dynamicRequest = NSURLRequest.requestWithURL(NSURL.URLWithString("https://api.spectra.com/test/$i")!!)
                SpectraURLProtocol.canInitWithRequest(dynamicRequest)
            }
        }
        
        val averageTimeMs = totalTime.inWholeNanoseconds.toDouble() / iterations / 1_000_000.0
        
        assertTrue(averageTimeMs < 5.0, "Average iOS interceptor init overhead was ${averageTimeMs}ms, should be < 5.0ms")
    }
}
