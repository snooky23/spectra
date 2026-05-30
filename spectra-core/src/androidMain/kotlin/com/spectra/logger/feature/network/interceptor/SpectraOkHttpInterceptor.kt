package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class SpectraOkHttpInterceptor(
    private val maxBodySize: Long = 250_000L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = IdGenerator.generate()
        val startTime = SpectraTime.now()

        // Extract Request Body
        var requestBodyText: String? = null
        try {
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                requestBodyText = buffer.readUtf8()
            }
        } catch (e: Exception) {
            requestBodyText = "[Failed to read request body: ${e.message}]"
        }

        val requestHeaders = extractHeaders(request.headers)

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            println("SpectraOkHttpInterceptor: Exception caught! ${e.message}")
            e.printStackTrace()
            val endTime = SpectraTime.now()
            val durationMs = kotlin.math.max(0L, endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds())

            val errorLog = NetworkLogEntry(
                id = requestId,
                timestamp = startTime,
                url = request.url.toString(),
                method = request.method,
                requestHeaders = requestHeaders,
                requestBody = requestBodyText,
                responseCode = null,
                responseHeaders = emptyMap(),
                responseBody = null,
                duration = durationMs,
                error = e.stackTraceToString(),
                source = "okhttp",
                sourceType = SourceType.PLUGIN
            )
            SpectraLogger.logNetwork(errorLog)
            throw e
        }

        val endTime = SpectraTime.now()
        val durationMs = kotlin.math.max(0L, endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds())

        // Extract Response Body safely using peekBody (max configurable limit)
        var responseBodyText: String? = null
        try {
            val peekedBody = response.peekBody(maxBodySize)
            responseBodyText = peekedBody.string()
        } catch (e: Exception) {
            responseBodyText = "[Failed to read response body: ${e.message}]"
        }

        val responseHeaders = extractHeaders(response.headers)

        val successLog = NetworkLogEntry(
            id = requestId,
            timestamp = startTime,
            url = request.url.toString(),
            method = request.method,
            requestHeaders = requestHeaders,
            requestBody = requestBodyText,
            responseCode = response.code,
            responseHeaders = responseHeaders,
            responseBody = responseBodyText,
            duration = durationMs,
            error = null,
            source = "okhttp",
            sourceType = SourceType.PLUGIN
        )
        println("SpectraOkHttpInterceptor: Logging success entry ${successLog.id}")
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
}
