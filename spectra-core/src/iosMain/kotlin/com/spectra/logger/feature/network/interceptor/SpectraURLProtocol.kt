package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.network.model.NetworkLogEntry
import platform.Foundation.*
import platform.darwin.NSObject
import kotlinx.cinterop.*
import kotlin.time.TimeSource

object SpectraIOSInterceptorConfig {
    var maxBodySize: Long = 250_000L
    var ignoreTokens: List<String> = emptyList()
    var ignoreRegex: List<Regex> = emptyList()
}

class SpectraURLProtocol(
    request: NSURLRequest,
    cachedResponse: NSCachedURLResponse?,
    client: NSURLProtocolClientProtocol?
) : NSURLProtocol(request, cachedResponse, client), NSURLSessionDataDelegateProtocol {

    private var dataTask: NSURLSessionDataTask? = null
    private var responseBodyData: NSMutableData = NSMutableData()
    private val requestId = IdGenerator.generate()
    private val startTime = SpectraTime.now()
    private val startMark = TimeSource.Monotonic.markNow()
    private var urlResponse: NSURLResponse? = null

    companion object : NSURLProtocolMeta() {
        override fun canInitWithRequest(request: NSURLRequest): Boolean {
            // Prevent infinite interception loop
            if (NSURLProtocol.propertyForKey("SpectraHandled", request) != null) {
                return false
            }

            val urlString = request.URL?.absoluteString ?: return false

            val shouldIgnore = SpectraIOSInterceptorConfig.ignoreTokens.any { urlString.contains(it, ignoreCase = true) } ||
                    SpectraIOSInterceptorConfig.ignoreRegex.any { it.containsMatchIn(urlString) }

            return !shouldIgnore
        }

        override fun canonicalRequestForRequest(request: NSURLRequest): NSURLRequest {
            return request
        }
    }

    override fun startLoading() {
        val mutableRequest = request.mutableCopy() as NSMutableURLRequest
        NSURLProtocol.setProperty(true, "SpectraHandled", mutableRequest)

        val session = NSURLSession.sessionWithConfiguration(
            NSURLSessionConfiguration.defaultSessionConfiguration,
            delegate = this,
            delegateQueue = null
        )
        dataTask = session.dataTaskWithRequest(mutableRequest)
        dataTask?.resume()
    }

    override fun stopLoading() {
        dataTask?.cancel()
        dataTask = null
    }

    // NSURLSessionDataDelegateProtocol methods

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveResponse: NSURLResponse,
        completionHandler: (NSURLSessionResponseDisposition) -> Unit
    ) {
        urlResponse = didReceiveResponse
        client?.URLProtocol(this, didReceiveResponse, NSURLCacheStoragePolicy.NSURLCacheStorageAllowed)
        completionHandler(NSURLSessionResponseAllow)
    }

    override fun URLSession(session: NSURLSession, dataTask: NSURLSessionDataTask, didReceiveData: NSData) {
        if (responseBodyData.length < SpectraIOSInterceptorConfig.maxBodySize.toULong()) {
            responseBodyData.appendData(didReceiveData)
        }
        client?.URLProtocol(this, didLoadData = didReceiveData)
    }

    override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
        val durationMs = startMark.elapsedNow().inWholeMilliseconds

        if (didCompleteWithError != null) {
            client?.URLProtocol(this, didFailWithError = didCompleteWithError)
            logFailed(didCompleteWithError, durationMs)
        } else {
            client?.URLProtocolDidFinishLoading(this)
            logSuccess(durationMs)
        }
    }

    private fun logSuccess(durationMs: Long) {
        val httpResponse = urlResponse as? NSHTTPURLResponse
        
        @Suppress("UNCHECKED_CAST")
        val responseHeaders = (httpResponse?.allHeaderFields as? Map<String, Any>)
            ?.mapKeys { it.key }
            ?.mapValues { it.value.toString() } 
            ?: emptyMap()

        @Suppress("UNCHECKED_CAST")
        val requestHeaders = (request.allHTTPHeaderFields as? Map<String, String>) 
            ?: emptyMap()

        var requestBodyText: String? = null
        request.HTTPBody?.let {
            requestBodyText = if (it.length > SpectraIOSInterceptorConfig.maxBodySize.toULong()) {
                "[Body exceeded limit]"
            } else {
                NSString.create(it, NSUTF8StringEncoding)?.toString() ?: "[Binary body omitted]"
            }
        }

        var responseBodyText: String? = null
        if (responseBodyData.length > 0u) {
            responseBodyText = if (responseBodyData.length >= SpectraIOSInterceptorConfig.maxBodySize.toULong()) {
                "[Body truncated]"
            } else {
                NSString.create(responseBodyData, NSUTF8StringEncoding)?.toString() ?: "[Binary body omitted]"
            }
        }

        val logEntry = NetworkLogEntry(
            id = requestId,
            timestamp = startTime,
            url = request.URL?.absoluteString ?: "Unknown",
            method = request.HTTPMethod ?: "GET",
            requestHeaders = requestHeaders,
            requestBody = requestBodyText,
            responseCode = httpResponse?.statusCode?.toInt(),
            responseHeaders = responseHeaders,
            responseBody = responseBodyText,
            duration = durationMs,
            error = null,
            source = "urlsession",
            sourceType = SourceType.PLUGIN
        )
        SpectraLogger.logNetwork(logEntry)
    }

    private fun logFailed(error: NSError, durationMs: Long) {
        @Suppress("UNCHECKED_CAST")
        val requestHeaders = (request.allHTTPHeaderFields as? Map<String, String>) 
            ?: emptyMap()

        val logEntry = NetworkLogEntry(
            id = requestId,
            timestamp = startTime,
            url = request.URL?.absoluteString ?: "Unknown",
            method = request.HTTPMethod ?: "GET",
            requestHeaders = requestHeaders,
            requestBody = null,
            responseCode = null,
            responseHeaders = emptyMap(),
            responseBody = null,
            duration = durationMs,
            error = error.localizedDescription,
            source = "urlsession",
            sourceType = SourceType.PLUGIN
        )
        SpectraLogger.logNetwork(logEntry)
    }
}
