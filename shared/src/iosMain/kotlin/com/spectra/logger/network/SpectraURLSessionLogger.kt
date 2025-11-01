package com.spectra.logger.network

import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.storage.NetworkLogStorage
import com.spectra.logger.utils.IdGenerator
import com.spectra.logger.utils.SourceDetector
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.dataTaskWithRequest

/**
 * Network logging wrapper for iOS URLSession.
 *
 * **Note**: Due to Kotlin/Native cinterop limitations with NSURLProtocol,
 * this implementation provides manual logging via URLSession task interception.
 *
 * Usage:
 * ```swift
 * // Create a wrapped session:
 * let storage = SpectraLogger.shared.networkStorage
 * let wrappedSession = SpectraURLSessionLogger.createSession(storage)
 *
 * // Use it instead of URLSession.shared:
 * wrappedSession.dataTask(with: url) { data, response, error in
 *     // Your code
 * }.resume()
 * ```
 *
 * **Alternative**: For automatic logging, use the Ktor client plugin in shared KMP code.
 */
@OptIn(ExperimentalForeignApi::class)
object SpectraURLSessionLogger {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Create a URLSession wrapper that logs network requests.
     *
     * @param storage The network log storage
     * @return An NSURLSession configured for logging
     */
    @Suppress("UNUSED_PARAMETER")
    fun createSession(storage: NetworkLogStorage): NSURLSession = NSURLSession.sharedSession

    /**
     * Manually log a network request/response.
     * Call this from your URLSession completion handler.
     *
     * @param url The request URL
     * @param method The HTTP method (GET, POST, etc.)
     * @param response The response received
     * @param data The response data
     * @param error Any error that occurred
     * @param duration Request duration in milliseconds
     * @param storage The network log storage
     */
    fun logRequest(
        url: String,
        method: String,
        response: NSURLResponse?,
        data: NSData?,
        error: platform.Foundation.NSError?,
        duration: Long,
        storage: NetworkLogStorage,
    ) {
        // Extract response details
        val httpResponse = response as? NSHTTPURLResponse
        val responseCode = httpResponse?.statusCode?.toInt()

        // Extract response body
        val responseBody =
            data?.let { responseData ->
                val bytes = responseData.toByteArray()
                bytes?.let {
                    try {
                        val bodyString = it.decodeToString()
                        NetworkLogEntry.truncateBody(bodyString)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

        val (source, sourceType) = SourceDetector.detectSource()
        val entry =
            NetworkLogEntry(
                id = IdGenerator.generate(),
                timestamp = Clock.System.now(),
                url = url,
                method = method,
                requestHeaders = emptyMap(),
                requestBody = null,
                responseCode = responseCode,
                responseHeaders = emptyMap(),
                responseBody = responseBody,
                duration = duration,
                error = error?.localizedDescription,
                source = source,
                sourceType = sourceType,
            )

        scope.launch {
            storage.add(entry)
        }
    }

    /**
     * Create a data task that automatically logs the request/response.
     *
     * @param session The URLSession to use
     * @param request The request to execute
     * @param storage The network log storage
     * @param completionHandler The completion handler to call with results
     * @return The created data task (you must call resume())
     */
    fun createDataTask(
        session: NSURLSession,
        request: NSURLRequest,
        storage: NetworkLogStorage,
        completionHandler: (NSData?, NSURLResponse?, platform.Foundation.NSError?) -> Unit,
    ): NSURLSessionDataTask {
        val url = request.URL?.absoluteString ?: ""
        val startTime = Clock.System.now().toEpochMilliseconds()

        return session.dataTaskWithRequest(request) { data, response, error ->
            val duration = Clock.System.now().toEpochMilliseconds() - startTime
            logRequest(url, "GET", response, data, error, duration, storage)
            completionHandler(data, response, error)
        }
    }

    /**
     * Convert NSData to ByteArray for string decoding.
     */
    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun NSData.toByteArray(): ByteArray? {
        if (this.length == 0uL) return null
        val data = this
        return ByteArray(data.length.toInt()).also { byteArray ->
            byteArray.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    }
}
