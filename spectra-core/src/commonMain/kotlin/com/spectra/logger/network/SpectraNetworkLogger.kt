package com.spectra.logger.network

import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.storage.NetworkLogStorage
import com.spectra.logger.utils.IdGenerator
import com.spectra.logger.utils.SourceDetector
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Ktor client plugin for network logging in KMP projects.
 *
 * Usage:
 * ```
 * val client = HttpClient {
 *     install(SpectraNetworkLogger) {
 *         storage = yourNetworkLogStorage
 *     }
 * }
 * ```
 */
class SpectraNetworkLoggerConfig {
    var storage: NetworkLogStorage? = null
}

val SpectraNetworkLogger: ClientPlugin<SpectraNetworkLoggerConfig> =
    createClientPlugin(
        "SpectraNetworkLogger",
        ::SpectraNetworkLoggerConfig,
    ) {
        val storage = pluginConfig.storage ?: error("NetworkLogStorage must be provided")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val startTimeKey = AttributeKey<Long>("SpectraStartTime")

        onRequest { request, _ ->
            request.attributes.put(startTimeKey, Clock.System.now().toEpochMilliseconds())
        }

        onResponse { response ->
            val startTime = response.call.request.attributes.getOrNull(startTimeKey) ?: 0L
            val duration = Clock.System.now().toEpochMilliseconds() - startTime

            val request = response.call.request

            // Extract request details
            val url = request.url.toString()
            val method = request.method.value
            val requestHeaders = request.headers.toMap()
            // Request body is not easily accessible in Ktor client plugin
            val requestBody: String? = null

            // Extract response details
            val responseCode = response.status.value
            val responseHeaders = response.headers.toMap()
            val responseBody =
                try {
                    NetworkLogEntry.truncateBody(response.bodyAsText())
                } catch (e: Exception) {
                    "[Unable to read body: ${e.message}]"
                }

            val (source, sourceType) = SourceDetector.detectSource()
            val entry =
                NetworkLogEntry(
                    id = IdGenerator.generate(),
                    timestamp = Clock.System.now(),
                    url = url,
                    method = method,
                    requestHeaders = requestHeaders,
                    requestBody = requestBody,
                    responseCode = responseCode,
                    responseHeaders = responseHeaders,
                    responseBody = responseBody,
                    duration = duration,
                    error = null,
                    source = source,
                    sourceType = sourceType,
                )

            scope.launch {
                storage.add(entry)
            }
        }
    }

/**
 * Convert Headers to Map.
 */
private fun Headers.toMap(): Map<String, String> =
    entries().associate { (key, values) ->
        key to values.joinToString(", ")
    }
