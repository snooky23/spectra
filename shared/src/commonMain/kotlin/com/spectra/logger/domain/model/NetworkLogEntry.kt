package com.spectra.logger.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a network request/response log entry.
 *
 * @property id Unique identifier
 * @property timestamp When the request was initiated
 * @property url Request URL
 * @property method HTTP method (GET, POST, etc.)
 * @property requestHeaders Request headers
 * @property requestBody Request body (truncated if too large)
 * @property responseCode HTTP response code (null if request failed)
 * @property responseHeaders Response headers
 * @property responseBody Response body (truncated if too large)
 * @property duration Request duration in milliseconds
 * @property error Error message if request failed
 */
@Serializable
data class NetworkLogEntry(
    val id: String,
    val timestamp: Instant,
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseCode: Int? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val duration: Long = 0,
    val error: String? = null,
) {
    /**
     * Indicates if the request was successful (2xx response code).
     */
    val isSuccessful: Boolean
        get() = responseCode in 200..299

    /**
     * Indicates if the request failed (no response or error).
     */
    val isFailed: Boolean
        get() = error != null || responseCode == null

    companion object {
        const val MAX_BODY_SIZE = 10_000 // 10KB max for body storage

        /**
         * Truncates body content if it exceeds max size.
         */
        fun truncateBody(body: String?): String? {
            if (body == null) return null
            if (body.length <= MAX_BODY_SIZE) return body
            return body.take(MAX_BODY_SIZE) + "\n... (truncated)"
        }
    }
}

/**
 * Filter criteria for network logs.
 *
 * @property methods Filter by HTTP methods
 * @property urlPattern Filter by URL pattern (contains)
 * @property statusCodes Filter by response status codes
 * @property minDuration Filter requests taking longer than this (ms)
 * @property showOnlyErrors Show only failed requests
 */
data class NetworkLogFilter(
    val methods: Set<String>? = null,
    val urlPattern: String? = null,
    val statusCodes: Set<Int>? = null,
    val minDuration: Long? = null,
    val showOnlyErrors: Boolean = false,
) {
    /**
     * Checks if a network log entry matches this filter.
     */
    fun matches(entry: NetworkLogEntry): Boolean {
        if (methods != null && entry.method !in methods) return false
        if (urlPattern != null && !entry.url.contains(urlPattern, ignoreCase = true)) return false
        if (statusCodes != null && entry.responseCode !in statusCodes) return false
        if (minDuration != null && entry.duration < minDuration) return false
        if (showOnlyErrors && !entry.isFailed) return false
        return true
    }

    companion object {
        /**
         * Filter that matches all entries.
         */
        val NONE = NetworkLogFilter()
    }
}
