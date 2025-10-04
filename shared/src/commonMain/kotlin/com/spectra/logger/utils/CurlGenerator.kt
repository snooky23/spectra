package com.spectra.logger.utils

import com.spectra.logger.domain.model.NetworkLogEntry

/**
 * Generates cURL command from network log entry
 */
object CurlGenerator {
    /**
     * Generate a cURL command that can be used to reproduce the network request
     *
     * @param entry The network log entry to convert
     * @return A formatted cURL command string
     */
    fun generate(entry: NetworkLogEntry): String {
        val parts = mutableListOf<String>()

        // Base curl command with URL
        parts.add("curl -X ${entry.method} '${entry.url}'")

        // Add request headers
        entry.requestHeaders.forEach { (key, value) ->
            parts.add("-H '$key: $value'")
        }

        // Add request body if present
        entry.requestBody?.let { body ->
            val escapedBody = body.replace("'", "'\\''")
            parts.add("-d '$escapedBody'")
        }

        // Join all parts with line continuation
        return parts.joinToString(" \\\n  ")
    }
}
