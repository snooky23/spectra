package com.spectra.logger.export

import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.NetworkLogFilter
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.domain.storage.NetworkLogStorage
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Export format for logs.
 */
enum class ExportFormat {
    TEXT,
    JSON,
    CSV,
}

/**
 * Utility for exporting logs to various formats.
 */
object LogExporter {
    /**
     * Export application logs to text format.
     *
     * @param storage The log storage
     * @param filter Optional filter to apply
     * @return Formatted log text
     */
    suspend fun exportLogsAsText(
        storage: LogStorage,
        filter: LogFilter = LogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("=== Spectra Logger Export ===")
            appendLine("Timestamp: ${formatTimestamp(kotlinx.datetime.Clock.System.now())}")
            appendLine("Total logs: ${logs.size}")
            appendLine()

            logs.forEach { log ->
                appendLine("${formatTimestamp(log.timestamp)} [${log.level.name}] ${log.tag}")
                appendLine("  ${log.message}")
                if (log.throwable != null) {
                    appendLine("  Error: ${log.throwable}")
                }
                if (log.metadata.isNotEmpty()) {
                    appendLine("  Metadata: ${log.metadata}")
                }
                appendLine()
            }
        }
    }

    /**
     * Export application logs to JSON format.
     *
     * @param storage The log storage
     * @param filter Optional filter to apply
     * @return JSON string
     */
    suspend fun exportLogsAsJson(
        storage: LogStorage,
        filter: LogFilter = LogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("{")
            appendLine("  \"exportTimestamp\": \"${kotlinx.datetime.Clock.System.now()}\",")
            appendLine("  \"totalLogs\": ${logs.size},")
            appendLine("  \"logs\": [")

            logs.forEachIndexed { index, log ->
                appendLine("    {")
                appendLine("      \"id\": \"${log.id}\",")
                appendLine("      \"timestamp\": \"${log.timestamp}\",")
                appendLine("      \"level\": \"${log.level.name}\",")
                appendLine("      \"tag\": \"${escapeJson(log.tag)}\",")
                appendLine("      \"message\": \"${escapeJson(log.message)}\",")
                appendLine(
                    "      \"throwable\": ${if (log.throwable != null) {
                        "\"${escapeJson(
                            log.throwable,
                        )}\""
                    } else {
                        "null"
                    }},",
                )
                append("      \"metadata\": {")
                log.metadata.entries.forEachIndexed { metaIndex, (key, value) ->
                    append("\"${escapeJson(key)}\": \"${escapeJson(value)}\"")
                    if (metaIndex < log.metadata.size - 1) append(", ")
                }
                appendLine("}")
                append("    }")
                if (index < logs.size - 1) appendLine(",")
            }

            appendLine()
            appendLine("  ]")
            appendLine("}")
        }
    }

    /**
     * Export application logs to CSV format.
     *
     * @param storage The log storage
     * @param filter Optional filter to apply
     * @return CSV string
     */
    suspend fun exportLogsAsCsv(
        storage: LogStorage,
        filter: LogFilter = LogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("Timestamp,Level,Tag,Message,Throwable")

            logs.forEach { log ->
                append(formatTimestamp(log.timestamp))
                append(",")
                append(log.level.name)
                append(",")
                append(escapeCsv(log.tag))
                append(",")
                append(escapeCsv(log.message))
                append(",")
                append(escapeCsv(log.throwable ?: ""))
                appendLine()
            }
        }
    }

    /**
     * Export network logs to text format.
     *
     * @param storage The network log storage
     * @param filter Optional filter to apply
     * @return Formatted log text
     */
    suspend fun exportNetworkLogsAsText(
        storage: NetworkLogStorage,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("=== Spectra Network Logger Export ===")
            appendLine("Timestamp: ${formatTimestamp(kotlinx.datetime.Clock.System.now())}")
            appendLine("Total requests: ${logs.size}")
            appendLine()

            logs.forEach { log ->
                appendLine("${formatTimestamp(log.timestamp)} ${log.method} ${log.url}")
                appendLine("  Status: ${log.responseCode ?: "ERROR"}")
                appendLine("  Duration: ${log.duration}ms")
                if (log.requestBody != null) {
                    appendLine("  Request Body: ${log.requestBody}")
                }
                if (log.responseBody != null) {
                    appendLine("  Response Body: ${log.responseBody}")
                }
                if (log.error != null) {
                    appendLine("  Error: ${log.error}")
                }
                appendLine()
            }
        }
    }

    /**
     * Export network logs to JSON format.
     *
     * @param storage The network log storage
     * @param filter Optional filter to apply
     * @return JSON string
     */
    suspend fun exportNetworkLogsAsJson(
        storage: NetworkLogStorage,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("{")
            appendLine("  \"exportTimestamp\": \"${kotlinx.datetime.Clock.System.now()}\",")
            appendLine("  \"totalRequests\": ${logs.size},")
            appendLine("  \"requests\": [")

            logs.forEachIndexed { index, log ->
                appendLine("    {")
                appendLine("      \"id\": \"${log.id}\",")
                appendLine("      \"timestamp\": \"${log.timestamp}\",")
                appendLine("      \"url\": \"${escapeJson(log.url)}\",")
                appendLine("      \"method\": \"${log.method}\",")
                appendLine("      \"responseCode\": ${log.responseCode ?: "null"},")
                appendLine("      \"duration\": ${log.duration},")
                appendLine(
                    "      \"requestBody\": ${if (log.requestBody != null) {
                        "\"${escapeJson(
                            log.requestBody,
                        )}\""
                    } else {
                        "null"
                    }},",
                )
                appendLine(
                    "      \"responseBody\": ${if (log.responseBody != null) {
                        "\"${escapeJson(
                            log.responseBody,
                        )}\""
                    } else {
                        "null"
                    }},",
                )
                appendLine("      \"error\": ${if (log.error != null) "\"${escapeJson(log.error)}\"" else "null"}")
                append("    }")
                if (index < logs.size - 1) appendLine(",")
            }

            appendLine()
            appendLine("  ]")
            appendLine("}")
        }
    }

    private fun formatTimestamp(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(
            2,
            '0',
        )}-${localDateTime.dayOfMonth.toString().padStart(2, '0')} " +
            "${localDateTime.hour.toString().padStart(
                2,
                '0',
            )}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
    }

    private fun escapeJson(str: String): String =
        str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

    private fun escapeCsv(str: String): String =
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            "\"${str.replace("\"", "\"\"")}\""
        } else {
            str
        }
}
