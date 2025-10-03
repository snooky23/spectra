package com.spectra.logger.network

import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.storage.NetworkLogStorage
import com.spectra.logger.utils.IdGenerator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURLConnection
import platform.Foundation.NSURLProtocol
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

/**
 * URLProtocol for iOS network logging.
 * Automatically captures all URLSession requests.
 *
 * Usage:
 * ```
 * // In your app initialization:
 * SpectraURLProtocol.register(storage)
 * ```
 *
 * @property storage Network log storage implementation
 */
@OptIn(ExperimentalForeignApi::class)
class SpectraURLProtocol private constructor(
    request: NSURLRequest,
    cachedResponse: platform.Foundation.NSCachedURLResponse?,
    client: platform.Foundation.NSURLProtocolClient?,
) : NSURLProtocol(request, cachedResponse, client) {
    private var connection: NSURLConnection? = null
    private var startTime: Long = 0
    private var responseData = mutableListOf<NSData>()

    companion object {
        private const val HANDLED_KEY = "SpectraURLProtocolHandled"
        private var storage: NetworkLogStorage? = null
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        /**
         * Register the protocol to intercept network requests.
         * Call this once during app initialization.
         */
        fun register(networkStorage: NetworkLogStorage) {
            storage = networkStorage
            platform.Foundation.NSURLProtocol.registerClass(SpectraURLProtocol::class)
        }

        /**
         * Unregister the protocol.
         */
        fun unregister() {
            platform.Foundation.NSURLProtocol.unregisterClass(SpectraURLProtocol::class)
            storage = null
        }
    }

    override fun canInitWithRequest(request: NSURLRequest): Boolean {
        // Don't handle requests we've already handled
        if (NSURLProtocol.propertyForKey(HANDLED_KEY, request) != null) {
            return false
        }
        // Only handle HTTP/HTTPS
        val scheme = request.URL?.scheme ?: return false
        return scheme == "http" || scheme == "https"
    }

    override fun canonicalRequestForRequest(request: NSURLRequest): NSURLRequest = request

    override fun startLoading() {
        startTime = Clock.System.now().toEpochMilliseconds()

        val mutableRequest =
            (request.mutableCopy() as NSMutableURLRequest).apply {
                NSURLProtocol.setProperty(true, HANDLED_KEY, this)
            }

        connection =
            NSURLConnection.connectionWithRequest(
                mutableRequest,
                this,
            )
    }

    override fun stopLoading() {
        connection?.cancel()
        connection = null
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun connection(
        connection: NSURLConnection,
        didReceiveResponse: NSURLResponse,
    ) {
        client?.URLProtocol(this, didReceiveResponse, platform.Foundation.NSURLCacheStorageAllowed)
        responseData.clear()
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun connection(
        connection: NSURLConnection,
        didReceiveData: NSData,
    ) {
        responseData.add(didReceiveData)
        client?.URLProtocol(this, didReceiveData)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun connectionDidFinishLoading(connection: NSURLConnection) {
        val duration = Clock.System.now().toEpochMilliseconds() - startTime
        logNetworkRequest(null, duration)
        client?.URLProtocolDidFinishLoading(this)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun connection(
        connection: NSURLConnection,
        didFailWithError: NSError,
    ) {
        val duration = Clock.System.now().toEpochMilliseconds() - startTime
        logNetworkRequest(didFailWithError.localizedDescription, duration)
        client?.URLProtocol(this, didFailWithError)
    }

    private fun logNetworkRequest(
        error: String?,
        duration: Long,
    ) {
        val currentStorage = storage ?: return

        val url = request.URL?.absoluteString ?: return
        val method = request.HTTPMethod ?: "GET"

        // Extract request headers
        val requestHeaders =
            request.allHTTPHeaderFields?.mapKeys { it.key.toString() }
                ?.mapValues { it.value.toString() } ?: emptyMap()

        // Extract request body
        val requestBody =
            request.HTTPBody?.let { data ->
                NSString.create(data, NSUTF8StringEncoding)?.toString()
            }

        // Extract response details
        val httpResponse = connection?.response as? NSHTTPURLResponse
        val responseCode = httpResponse?.statusCode?.toInt()

        val responseHeaders =
            httpResponse?.allHeaderFields?.mapKeys { it.key.toString() }
                ?.mapValues { it.value.toString() } ?: emptyMap()

        // Combine response data
        val responseBody =
            if (responseData.isNotEmpty()) {
                // Concatenate all data chunks
                val combined =
                    responseData.joinToString("") { data ->
                        NSString.create(data, NSUTF8StringEncoding)?.toString() ?: ""
                    }
                NetworkLogEntry.truncateBody(combined)
            } else {
                null
            }

        val entry =
            NetworkLogEntry(
                id = IdGenerator.generate(),
                timestamp = Clock.System.now(),
                url = url,
                method = method,
                requestHeaders = requestHeaders,
                requestBody = NetworkLogEntry.truncateBody(requestBody),
                responseCode = responseCode,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                duration = duration,
                error = error,
            )

        scope.launch {
            currentStorage.add(entry)
        }
    }
}
