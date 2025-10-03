package com.spectra.logger.network

import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.storage.NetworkLogStorage
import com.spectra.logger.utils.IdGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * OkHttp interceptor for Android network logging.
 * Automatically captures all HTTP requests and responses.
 *
 * Usage:
 * ```
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(SpectraNetworkInterceptor(storage))
 *     .build()
 * ```
 *
 * @property storage Network log storage implementation
 */
class SpectraNetworkInterceptor(
    private val storage: NetworkLogStorage,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        // Capture request details
        val requestHeaders =
            request.headers.toMultimap().mapValues { it.value.joinToString(", ") }
        val requestBody =
            request.body?.let { body ->
                try {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    NetworkLogEntry.truncateBody(buffer.readUtf8())
                } catch (e: Exception) {
                    "[Unable to read body: ${e.message}]"
                }
            }

        var response: Response? = null
        var error: String? = null

        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            error = e.message ?: "Network error"
        } catch (e: Exception) {
            error = e.message ?: "Unknown error"
        }

        val duration = System.currentTimeMillis() - startTime

        // Capture response details
        val responseCode = response?.code
        val responseHeaders =
            response?.headers?.toMultimap()?.mapValues { it.value.joinToString(", ") }
                ?: emptyMap()
        val responseBody =
            response?.peekBody(NetworkLogEntry.MAX_BODY_SIZE.toLong())?.let { body ->
                try {
                    NetworkLogEntry.truncateBody(body.string())
                } catch (e: Exception) {
                    "[Unable to read body: ${e.message}]"
                }
            }

        // Log the network entry
        val entry =
            NetworkLogEntry(
                id = IdGenerator.generate(),
                timestamp = Clock.System.now(),
                url = request.url.toString(),
                method = request.method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                responseCode = responseCode,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                duration = duration,
                error = error,
            )

        scope.launch {
            storage.add(entry)
        }

        // Re-throw exception or return response
        if (response != null) {
            return response
        } else {
            throw IOException(error ?: "Network request failed")
        }
    }
}
