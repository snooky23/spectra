package com.spectra.logger.feature.network.interceptor

import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SourceDetector
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.storage.NetworkLogStorage
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
            request.attributes.put(startTimeKey, com.spectra.logger.core.utils.SpectraTime.now().toEpochMilliseconds())
        }

        onResponse { response ->
            val startTime = response.call.request.attributes.getOrNull(startTimeKey) ?: 0L
            val duration = com.spectra.logger.core.utils.SpectraTime.now().toEpochMilliseconds() - startTime

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
                    timestamp = com.spectra.logger.core.utils.SpectraTime.now(),
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
