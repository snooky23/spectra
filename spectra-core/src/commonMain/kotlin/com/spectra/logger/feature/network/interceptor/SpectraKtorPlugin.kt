package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.network.model.NetworkLogEntry
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.content.TextContent
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.util.toMap
import kotlinx.coroutines.CancellationException
import kotlin.time.TimeSource

class SpectraKtorConfig {
    /**
     * Maximum number of bytes to capture for request/response bodies.
     * Defaults to 250KB (industry standard for network interceptors).
     */
    var maxBodySize: Long = 250_000L
    
    // Additional configuration properties for future stories (e.g. ignoreList)
}

/**
 * Ktor Client Plugin for intercepting and logging network traffic to Spectra.
 */
val SpectraKtorPlugin = createClientPlugin("SpectraKtorPlugin", ::SpectraKtorConfig) {
    on(Send) { request ->
        val startTime = SpectraTime.now()
        val startMark = TimeSource.Monotonic.markNow()
        val requestId = IdGenerator.generate()
        
        // Attempt to safely extract the request body without consuming a stream
        var requestBodyText: String? = null
        val body = request.body
        when (body) {
            is TextContent -> requestBodyText = body.text
            is ByteArrayContent -> requestBodyText = "[ByteArray: ${body.contentLength} bytes]"
            is OutgoingContent.NoContent -> requestBodyText = null
            io.ktor.client.utils.EmptyContent -> requestBodyText = null
            is OutgoingContent.ProtocolUpgrade -> requestBodyText = "[Protocol Upgrade]"
            is OutgoingContent.ReadChannelContent -> requestBodyText = "[Stream: ReadChannelContent]"
            is OutgoingContent.WriteChannelContent -> requestBodyText = "[Stream: WriteChannelContent]"
            else -> requestBodyText = "[Body type: ${body::class.simpleName}]"
        }

        val requestHeaders = request.headers.entries().associate { 
            it.key to it.value.joinToString(", ") 
        }

        try {
            // Proceed with the actual network call
            val call = proceed(request)
            val response = call.response
            val durationMs = startMark.elapsedNow().inWholeMilliseconds
            
            val responseHeaders = response.headers.entries().associate { 
                it.key to it.value.joinToString(", ") 
            }
            
            // Note: Safely intercepting response bodies in Ktor without consuming the channel 
            // requires stream buffering (e.g. DoubleReceive plugin). For now, we log the metadata and size.
            val responseBodyText = "[Response body capture pending DoubleReceive configuration]"

            val logEntry = NetworkLogEntry(
                id = requestId,
                timestamp = startTime,
                url = request.url.toString(),
                method = request.method.value,
                requestHeaders = requestHeaders,
                requestBody = NetworkLogEntry.truncateBody(requestBodyText),
                responseCode = response.status.value,
                responseHeaders = responseHeaders,
                responseBody = NetworkLogEntry.truncateBody(responseBodyText),
                duration = durationMs,
                error = null,
                source = "ktor",
                sourceType = SourceType.PLUGIN
            )

            SpectraLogger.logNetwork(logEntry)
            return@on call
        } catch (e: Throwable) {
            val isCancelled = e is CancellationException
            val durationMs = startMark.elapsedNow().inWholeMilliseconds
            
            val logEntry = NetworkLogEntry(
                id = requestId,
                timestamp = startTime,
                url = request.url.toString(),
                method = request.method.value,
                requestHeaders = requestHeaders,
                requestBody = NetworkLogEntry.truncateBody(requestBodyText),
                responseCode = null,
                responseHeaders = emptyMap(),
                responseBody = null,
                duration = durationMs,
                error = if (isCancelled) "Cancelled" else e.stackTraceToString(),
                source = "ktor",
                sourceType = SourceType.PLUGIN
            )
            
            SpectraLogger.logNetwork(logEntry)
            throw e
        }
    }
}
