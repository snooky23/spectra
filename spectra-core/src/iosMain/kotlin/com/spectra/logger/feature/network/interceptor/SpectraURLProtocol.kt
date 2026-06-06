package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.network.model.NetworkLogEntry
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.*
import platform.Foundation.*
import kotlin.time.TimeSource

/**
 * Global configuration options for the native iOS `SpectraURLProtocol` interceptor.
 *
 * Since this config is used directly from Swift via `SpectraIOSInterceptorConfig.shared`,
 * properties are backed by thread-safe `atomic` references to ensure concurrent URLSessions
 * don't trigger race conditions.
 */
object SpectraIOSInterceptorConfig {
    /**
     * The maximum body size (in bytes) to capture for a network payload.
     * Payloads exceeding this limit will be truncated.
     * If -1, falls back to `SpectraLogger.configuration`.
     */
    private val _maxBodySize = atomic<Long>(-1L)
    var maxBodySize: Long
        get() = _maxBodySize.value
        set(value) {
            _maxBodySize.value = value
        }

    /**
     * A list of substrings. If a request URL contains any of these substrings, it is ignored.
     * If empty, falls back to `SpectraLogger.configuration`.
     */
    private val _ignoreTokens = atomic<List<String>>(emptyList())
    var ignoreTokens: List<String>
        get() = _ignoreTokens.value
        set(value) {
            _ignoreTokens.value = value
        }

    /**
     * A list of regular expressions. If a request URL matches any regex,
     * it will be completely ignored by the logger.
     */
    private val _ignoreRegex = atomic<List<Regex>>(emptyList())
    var ignoreRegex: List<Regex>
        get() = _ignoreRegex.value
        set(value) {
            _ignoreRegex.value = value
        }

    internal val currentMaxBodySize: Long
        get() {
            val max = maxBodySize
            return (if (max >= 0L) max else SpectraLogger.configuration.performanceConfig.maxBodySize.toLong()).coerceAtLeast(0L)
        }

    internal val currentIgnoreTokens: List<String>
        get() {
            val tokens = ignoreTokens
            return if (tokens.isNotEmpty()) {
                tokens
            } else {
                SpectraLogger.configuration.enabledFeatures.networkIgnoredTokens +
                    SpectraLogger.configuration.enabledFeatures.networkIgnoredDomains
            }
        }
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
class SpectraURLProtocol(
    request: NSURLRequest,
    cachedResponse: NSCachedURLResponse?,
    client: NSURLProtocolClientProtocol?,
) : NSURLProtocol(request, cachedResponse, client), NSURLSessionDataDelegateProtocol {
    private var dataTask: NSURLSessionDataTask? = null
    private var urlSession: NSURLSession? = null
    private var responseBodyData: NSMutableData = NSMutableData()
    private val requestId = IdGenerator.generate()
    private val startTime = SpectraTime.now()
    private val startMark = TimeSource.Monotonic.markNow()
    private var urlResponse: NSURLResponse? = null
    private var isResponseTruncated = false

    companion object : NSURLProtocolMeta() {
        override fun canInitWithRequest(request: NSURLRequest): Boolean {
            // Prevent infinite interception loop
            if (NSURLProtocol.propertyForKey("SpectraHandled", request) != null) {
                return false
            }

            val urlString = request.URL?.absoluteString ?: return false
            val currentTokens = SpectraIOSInterceptorConfig.currentIgnoreTokens

            val shouldIgnore =
                currentTokens.any { urlString.contains(it, ignoreCase = true) } ||
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

        urlSession =
            NSURLSession.sessionWithConfiguration(
                NSURLSessionConfiguration.defaultSessionConfiguration,
                delegate = this,
                delegateQueue = null,
            )
        dataTask = urlSession?.dataTaskWithRequest(mutableRequest)
        dataTask?.resume()
    }

    override fun stopLoading() {
        dataTask?.cancel()
        urlSession?.invalidateAndCancel()
        urlSession = null
        dataTask = null
    }

    // NSURLSessionDataDelegateProtocol methods

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        willPerformHTTPRedirection: NSHTTPURLResponse,
        newRequest: NSURLRequest,
        completionHandler: (NSURLRequest?) -> Unit,
    ) {
        client?.URLProtocol(this, wasRedirectedToRequest = newRequest, redirectResponse = willPerformHTTPRedirection)
        completionHandler(newRequest)
    }

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveResponse: NSURLResponse,
        completionHandler: (NSURLSessionResponseDisposition) -> Unit,
    ) {
        urlResponse = didReceiveResponse
        client?.URLProtocol(this, didReceiveResponse, NSURLCacheStoragePolicy.NSURLCacheStorageAllowed)
        completionHandler(NSURLSessionResponseAllow)
    }

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveData: NSData,
    ) {
        val currentSize = responseBodyData.length.toLong()
        val maxSize = SpectraIOSInterceptorConfig.currentMaxBodySize
        if (currentSize < maxSize) {
            val remaining = maxSize - currentSize
            if (didReceiveData.length.toLong() <= remaining) {
                responseBodyData.appendData(didReceiveData)
            } else {
                val subdata = didReceiveData.subdataWithRange(NSMakeRange(0uL, remaining.toULong()))
                responseBodyData.appendData(subdata)
                isResponseTruncated = true
            }
        } else {
            isResponseTruncated = true
        }
        client?.URLProtocol(this, didLoadData = didReceiveData)
    }

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didCompleteWithError: NSError?,
    ) {
        val durationMs = startMark.elapsedNow().inWholeMilliseconds

        if (didCompleteWithError != null) {
            client?.URLProtocol(this, didFailWithError = didCompleteWithError)
            logFailed(didCompleteWithError, durationMs)
        } else {
            client?.URLProtocolDidFinishLoading(this)
            logSuccess(durationMs)
        }
        urlSession?.finishTasksAndInvalidate()
        urlSession = null
    }

    private fun logSuccess(durationMs: Long) {
        val httpResponse = urlResponse as? NSHTTPURLResponse

        @Suppress("UNCHECKED_CAST")
        val responseHeadersRaw = httpResponse?.allHeaderFields as? Map<Any?, Any?> ?: emptyMap()
        val responseHeaders =
            responseHeadersRaw.entries.associate { (k, v) ->
                val keyStr = k.toString()
                val valStr =
                    if (v is List<*>) {
                        v.joinToString(", ") { it.toString() }
                    } else {
                        v.toString()
                    }
                keyStr to valStr
            }

        @Suppress("UNCHECKED_CAST")
        val requestHeaders =
            (request.allHTTPHeaderFields as? Map<String, String>)
                ?: emptyMap()

        var requestBodyText: String? = null
        request.HTTPBody?.let {
            val maxSize = SpectraIOSInterceptorConfig.currentMaxBodySize.toULong()
            requestBodyText =
                if (it.length > maxSize) {
                    val subdata = it.subdataWithRange(NSMakeRange(0uL, maxSize))
                    var str = NSString.create(subdata, NSUTF8StringEncoding)?.toString()
                    if (str == null) {
                        for (drop in 1..3) {
                            val fbLen = (maxSize.toLong() - drop).coerceAtLeast(0L).toULong()
                            if (fbLen > 0u) {
                                str = NSString.create(it.subdataWithRange(NSMakeRange(0uL, fbLen)), NSUTF8StringEncoding)?.toString()
                                if (str != null) break
                            }
                        }
                    }
                    val finalStr = str ?: "[Binary body omitted]"
                    "$finalStr\n[Body truncated]"
                } else {
                    NSString.create(it, NSUTF8StringEncoding)?.toString() ?: "[Binary body omitted]"
                }
        } ?: request.HTTPBodyStream?.let {
            requestBodyText = "[Stream body omitted]"
        }

        var responseBodyText: String? = null
        if (responseBodyData.length > 0u || isResponseTruncated) {
            var str: String? = null
            if (responseBodyData.length > 0u) {
                // To avoid inefficient loops on huge NSData, try decode once
                str = NSString.create(responseBodyData, NSUTF8StringEncoding)?.toString()
                if (str == null && isResponseTruncated) {
                    // Try removing up to 3 bytes from the END of the data
                    val len = responseBodyData.length.toLong()
                    for (drop in 1..3) {
                        val fbLen = (len - drop).coerceAtLeast(0L).toULong()
                        if (fbLen > 0u) {
                            val sub = responseBodyData.subdataWithRange(NSMakeRange(0uL, fbLen))
                            str = NSString.create(sub, NSUTF8StringEncoding)?.toString()
                            if (str != null) break
                        }
                    }
                }
            }
            val finalStr = str ?: if (responseBodyData.length > 0u) "[Binary body omitted]" else ""
            responseBodyText = if (isResponseTruncated) {
                if (finalStr.isEmpty()) "\n[Body truncated]" else "$finalStr\n[Body truncated]"
            } else finalStr
        }

        val logEntry =
            NetworkLogEntry(
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
                sourceType = SourceType.PLUGIN,
            )
        SpectraLogger.logNetwork(logEntry)
    }

    private fun logFailed(
        error: NSError,
        durationMs: Long,
    ) {
        @Suppress("UNCHECKED_CAST")
        val requestHeaders =
            (request.allHTTPHeaderFields as? Map<String, String>)
                ?: emptyMap()

        val logEntry =
            NetworkLogEntry(
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
                sourceType = SourceType.PLUGIN,
            )
        SpectraLogger.logNetwork(logEntry)
    }
}
