package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.network.model.NetworkLogEntry
import io.ktor.client.call.save
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.contentLength
import kotlinx.coroutines.CancellationException
import kotlin.time.TimeSource

class SpectraKtorConfig {
    var maxBodySize: Long? = null
    var ignoreTokens: List<String>? = null
    var ignoreRegex: List<Regex> = emptyList()
}

val SpectraKtorPlugin =
    createClientPlugin("SpectraKtorPlugin", ::SpectraKtorConfig) {
        on(Send) { request ->
            val urlString = request.url.toString()
            val localTokens = pluginConfig.ignoreTokens
            val shouldIgnore =
                localTokens?.any { urlString.contains(it, ignoreCase = true) } == true ||
                    (
                        localTokens == null &&
                            runCatching {
                                val config = SpectraLogger.configuration.enabledFeatures
                                config.networkIgnoredTokens.any { urlString.contains(it, ignoreCase = true) } ||
                                    config.networkIgnoredDomains.any { urlString.contains(it, ignoreCase = true) }
                            }.getOrDefault(false)
                    ) || pluginConfig.ignoreRegex.any { it.containsMatchIn(urlString) }

            if (shouldIgnore) {
                return@on proceed(request)
            }

            val startTime = SpectraTime.now()
            val startMark = TimeSource.Monotonic.markNow()
            val requestId = IdGenerator.generate()

            var requestBodyText: String? = null
            val body = request.body
            val currentMaxBodySize =
                runCatching {
                    (pluginConfig.maxBodySize ?: SpectraLogger.configuration.performanceConfig.maxBodySize.toLong()).coerceAtLeast(0L)
                }.getOrDefault(1024L * 1024L)

            fun truncateSafely(text: String): String {
                val bytes = text.encodeToByteArray()
                if (bytes.size <= currentMaxBodySize) return text
                val limit = currentMaxBodySize.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                return bytes.decodeToString(endIndex = limit) + "\n[Body truncated]"
            }

            when (body) {
                is TextContent -> requestBodyText = truncateSafely(body.text)
                is ByteArrayContent -> requestBodyText = "[ByteArray: ${body.contentLength} bytes]"
                is OutgoingContent.NoContent -> requestBodyText = null
                io.ktor.client.utils.EmptyContent -> requestBodyText = null
                is OutgoingContent.ProtocolUpgrade -> requestBodyText = "[Protocol Upgrade]"
                is OutgoingContent.ReadChannelContent -> requestBodyText = "[Stream: ReadChannelContent]"
                is OutgoingContent.WriteChannelContent -> requestBodyText = "[Stream: WriteChannelContent]"
                else -> requestBodyText = "[Body type: ${body::class.simpleName}]"
            }

            val requestHeaders =
                request.headers.entries().associate {
                    it.key to it.value.joinToString(", ")
                }

            try {
                val call = proceed(request)
                val response = call.response
                val durationMs = startMark.elapsedNow().inWholeMilliseconds

                val responseHeaders =
                    response.headers.entries().associate {
                        it.key to it.value.joinToString(", ")
                    }

                val contentLength = response.contentLength() ?: -1L
                val savedCall =
                    if (contentLength in 0..currentMaxBodySize) {
                        call.save()
                    } else {
                        call
                    }

                val responseBodyText =
                    if (savedCall === call && contentLength > currentMaxBodySize) {
                        "[Response body exceeded $currentMaxBodySize bytes limit]"
                    } else if (savedCall === call && contentLength < 0L) {
                        "[Chunked response body omitted to prevent stream consumption]"
                    } else {
                        val raw = runCatching {
                            savedCall.response.bodyAsText()
                        }.getOrElse { "[Failed to read response body: ${it.message}]" }
                        truncateSafely(raw)
                    }

                val logEntry =
                    NetworkLogEntry(
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
                        sourceType = SourceType.PLUGIN,
                    )

                SpectraLogger.logNetwork(logEntry)
                return@on savedCall
            } catch (e: Throwable) {
                val isCancelled = e is CancellationException
                val durationMs = startMark.elapsedNow().inWholeMilliseconds

                val logEntry =
                    NetworkLogEntry(
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
                        sourceType = SourceType.PLUGIN,
                    )

                SpectraLogger.logNetwork(logEntry)
                throw e
            }
        }
    }
