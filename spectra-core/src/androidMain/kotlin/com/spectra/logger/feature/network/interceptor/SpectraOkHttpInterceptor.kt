package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okio.Buffer

class SpectraOkHttpInterceptor(
    private val maxBodySize: Long = 250_000L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = IdGenerator.generate()
        val startTime = SpectraTime.now()
        val startNano = System.nanoTime()

        // Extract Request Body
        var requestBodyText: String? = null
        try {
            request.body?.let { body ->
                if (body.isOneShot()) {
                    requestBodyText = "[One-shot body omitted]"
                } else if (body.isDuplex()) {
                    requestBodyText = "[Duplex body omitted]"
                } else if (!isPlainText(body.contentType())) {
                    requestBodyText = "[Binary body omitted]"
                } else if (body.contentLength() > maxBodySize) {
                    requestBodyText = "[Body exceeded $maxBodySize bytes limit]"
                } else {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    requestBodyText = buffer.readUtf8()
                }
            }
        } catch (e: Throwable) {
            requestBodyText = "[Failed to read request body: ${e.message}]"
        }

        val requestHeaders = extractHeaders(request.headers)

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Throwable) {
            val durationMs = kotlin.math.max(0L, (System.nanoTime() - startNano) / 1_000_000L)
            
            val isCancelled = chain.call().isCanceled()
            val errorMessage = if (isCancelled) "Cancelled" else e.stackTraceToString()

            val errorLog = NetworkLogEntry(
                id = requestId,
                timestamp = startTime,
                url = request.url.toString(),
                method = request.method,
                requestHeaders = requestHeaders,
                requestBody = NetworkLogEntry.truncateBody(requestBodyText),
                responseCode = null,
                responseHeaders = emptyMap(),
                responseBody = null,
                duration = durationMs,
                error = errorMessage,
                source = "okhttp",
                sourceType = SourceType.PLUGIN
            )
            SpectraLogger.logNetwork(errorLog)
            throw e
        }

        val durationMs = kotlin.math.max(0L, (System.nanoTime() - startNano) / 1_000_000L)

        // Extract Response Body safely using peekBody (max configurable limit)
        var responseBodyText: String? = null
        try {
            if (!isPlainText(response.body?.contentType())) {
                responseBodyText = "[Binary body omitted]"
            } else {
                val peekedBody = response.peekBody(maxBodySize)
                responseBodyText = peekedBody.string()
            }
        } catch (e: Throwable) {
            responseBodyText = "[Failed to read response body: ${e.message}]"
        }

        val responseHeaders = extractHeaders(response.headers)

        val successLog = NetworkLogEntry(
            id = requestId,
            timestamp = startTime,
            url = request.url.toString(),
            method = request.method,
            requestHeaders = requestHeaders,
            requestBody = NetworkLogEntry.truncateBody(requestBodyText),
            responseCode = response.code,
            responseHeaders = responseHeaders,
            responseBody = NetworkLogEntry.truncateBody(responseBodyText),
            duration = durationMs,
            error = null,
            source = "okhttp",
            sourceType = SourceType.PLUGIN
        )
        SpectraLogger.logNetwork(successLog)
        
        return response
    }

    private fun extractHeaders(headers: Headers): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            val value = headers.value(i)
            map[name] = if (map.containsKey(name)) {
                map[name] + ", " + value
            } else {
                value
            }
        }
        return map
    }
    
    private fun isPlainText(mediaType: MediaType?): Boolean {
        if (mediaType == null) return false
        if (mediaType.type == "text") return true
        val subtype = mediaType.subtype
        return subtype == "json" || subtype == "xml" || subtype == "html" || subtype == "x-www-form-urlencoded"
    }
}
