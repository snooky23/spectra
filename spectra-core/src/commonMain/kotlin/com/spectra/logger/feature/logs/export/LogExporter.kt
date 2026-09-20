package com.spectra.logger.feature.logs.export

import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.storage.EventLogStorage
import com.spectra.logger.feature.logs.model.LogFilter
import com.spectra.logger.feature.logs.storage.LogStorage
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.feature.network.storage.NetworkLogStorage
import com.spectra.logger.feature.streaming.model.DeviceInfo
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Supported export formats for Spectra telemetry.
 */
enum class ExportFormat {
    TEXT,
    JSON,
    CSV,
    HAR,
    MARKDOWN,
}

/**
 * Utility for exporting application logs, network telemetry, and events
 * to industry-standard formats including HAR 1.2, JSON, CSV, and Markdown.
 */
object LogExporter {
    /**
     * Export application logs to plain text format.
     */
    suspend fun exportLogsAsText(
        storage: LogStorage,
        filter: LogFilter = LogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("=== Spectra Logger Export ===")
            appendLine("Timestamp: ${formatTimestamp(SpectraTime.now())}")
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
     */
    suspend fun exportLogsAsJson(
        storage: LogStorage,
        filter: LogFilter = LogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("{")
            appendLine("  \"exportTimestamp\": \"${SpectraTime.now()}\",")
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
                        "\"${escapeJson(log.throwable)}\""
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
     * Export application logs to Markdown format.
     */
    suspend fun exportLogsAsMarkdown(
        storage: LogStorage,
        filter: LogFilter = LogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("# Spectra Application Logs Report")
            appendLine("- **Generated**: ${formatTimestamp(SpectraTime.now())}")
            appendLine("- **Total Entries**: ${logs.size}")
            appendLine()
            appendLine("| Time | Level | Tag | Message |")
            appendLine("|---|---|---|---|")
            logs.forEach { log ->
                appendLine(
                    "| ${formatTimestamp(log.timestamp)} | `${log.level.name}` | " +
                        "`${escapeMarkdownCell(log.tag)}` | ${escapeMarkdownCell(log.message)} |",
                )
            }
        }
    }

    /**
     * Export network logs to text format.
     */
    suspend fun exportNetworkLogsAsText(
        storage: NetworkLogStorage,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("=== Spectra Network Logger Export ===")
            appendLine("Timestamp: ${formatTimestamp(SpectraTime.now())}")
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
     */
    suspend fun exportNetworkLogsAsJson(
        storage: NetworkLogStorage,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("{")
            appendLine("  \"exportTimestamp\": \"${SpectraTime.now()}\",")
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
                        "\"${escapeJson(log.requestBody)}\""
                    } else {
                        "null"
                    }},",
                )
                appendLine(
                    "      \"responseBody\": ${if (log.responseBody != null) {
                        "\"${escapeJson(log.responseBody)}\""
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

    /**
     * Export network logs in HTTP Archive (HAR 1.2) format.
     * Compatible with Chrome DevTools, Charles Proxy, Proxyman, and Postman.
     */
    suspend fun exportNetworkLogsAsHar(
        storage: NetworkLogStorage,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("{")
            appendLine("  \"log\": {")
            appendLine("    \"version\": \"1.2\",")
            appendLine("    \"creator\": {")
            appendLine("      \"name\": \"Spectra Logger\",")
            appendLine("      \"version\": \"1.0.0\"")
            appendLine("    },")
            appendLine("    \"entries\": [")

            logs.forEachIndexed { index, log ->
                val (queryStringJson, headersJson, resHeadersJson) = formatHarHeaders(log)
                appendLine("      {")
                appendLine("        \"startedDateTime\": \"${log.timestamp}\",")
                appendLine("        \"time\": ${log.duration},")
                appendLine("        \"request\": {")
                appendLine("          \"method\": \"${escapeJson(log.method)}\",")
                appendLine("          \"url\": \"${escapeJson(log.url)}\",")
                appendLine("          \"httpVersion\": \"HTTP/1.1\",")
                appendLine("          \"cookies\": [],")
                appendLine("          \"headers\": $headersJson,")
                appendLine("          \"queryString\": $queryStringJson,")
                if (log.requestBody != null) {
                    appendLine("          \"postData\": {")
                    appendLine("            \"mimeType\": \"application/json\",")
                    appendLine("            \"text\": \"${escapeJson(log.requestBody)}\"")
                    appendLine("          },")
                }
                appendLine("          \"headersSize\": -1,")
                appendLine("          \"bodySize\": ${log.requestBody?.length ?: 0}")
                appendLine("        },")
                appendLine("        \"response\": {")
                val status = log.responseCode ?: 0
                val statusText =
                    when {
                        status in 200..299 -> "OK"
                        status != 0 -> "Status $status"
                        else -> "Error"
                    }
                appendLine("          \"status\": $status,")
                appendLine("          \"statusText\": \"$statusText\",")
                appendLine("          \"httpVersion\": \"HTTP/1.1\",")
                appendLine("          \"cookies\": [],")
                appendLine("          \"headers\": $resHeadersJson,")
                appendLine("          \"content\": {")
                appendLine("            \"size\": ${log.responseBody?.length ?: 0},")
                appendLine("            \"mimeType\": \"application/json\",")
                appendLine("            \"text\": \"${escapeJson(log.responseBody.orEmpty())}\"")
                appendLine("          },")
                appendLine("          \"redirectURL\": \"\",")
                appendLine("          \"headersSize\": -1,")
                appendLine("          \"bodySize\": ${log.responseBody?.length ?: 0}")
                appendLine("        },")
                appendLine("        \"cache\": {},")
                appendLine("        \"timings\": {")
                appendLine("          \"send\": 0,")
                appendLine("          \"wait\": ${log.duration},")
                appendLine("          \"receive\": 0")
                appendLine("        }")
                append("      }")
                if (index < logs.size - 1) appendLine(",")
            }

            appendLine()
            appendLine("    ]")
            appendLine("  }")
            appendLine("}")
        }
    }

    /**
     * Export discrete analytics/user events to plain text format.
     */
    suspend fun exportEventsAsText(
        storage: EventLogStorage,
        filter: EventFilter = EventFilter.NONE,
    ): String {
        val events = storage.query(filter)
        return buildString {
            appendLine("=== Spectra Events Export ===")
            appendLine("Timestamp: ${formatTimestamp(SpectraTime.now())}")
            appendLine("Total events: ${events.size}")
            appendLine()

            events.forEach { event ->
                val duration = if (event.durationMs != null) " (${event.durationMs}ms)" else ""
                appendLine("${formatTimestamp(event.timestamp)} [${event.eventType.name}] ${event.name}$duration")
                appendLine("  Source: ${event.source} (${event.sourceType})")
                if (event.parameters.isNotEmpty()) {
                    appendLine("  Parameters: ${event.parameters}")
                }
                appendLine()
            }
        }
    }

    /**
     * Export discrete analytics/user events to JSON format.
     */
    suspend fun exportEventsAsJson(
        storage: EventLogStorage,
        filter: EventFilter = EventFilter.NONE,
    ): String {
        val events = storage.query(filter)
        return buildString {
            appendLine("{")
            appendLine("  \"exportTimestamp\": \"${SpectraTime.now()}\",")
            appendLine("  \"totalEvents\": ${events.size},")
            appendLine("  \"events\": [")

            events.forEachIndexed { index, event ->
                appendLine("    {")
                appendLine("      \"id\": \"${event.id}\",")
                appendLine("      \"timestamp\": \"${event.timestamp}\",")
                appendLine("      \"type\": \"${event.eventType.name}\",")
                appendLine("      \"name\": \"${escapeJson(event.name)}\",")
                appendLine("      \"durationMs\": ${event.durationMs ?: "null"},")
                appendLine("      \"source\": \"${escapeJson(event.source)}\",")
                appendLine("      \"sourceType\": \"${event.sourceType.name}\",")
                append("      \"parameters\": {")
                event.parameters.entries.forEachIndexed { paramIdx, (k, v) ->
                    append("\"${escapeJson(k)}\": \"${escapeJson(v)}\"")
                    if (paramIdx < event.parameters.size - 1) append(", ")
                }
                appendLine("}")
                append("    }")
                if (index < events.size - 1) appendLine(",")
            }

            appendLine()
            appendLine("  ]")
            appendLine("}")
        }
    }

    /**
     * Export discrete analytics/user events to CSV format.
     */
    suspend fun exportEventsAsCsv(
        storage: EventLogStorage,
        filter: EventFilter = EventFilter.NONE,
    ): String {
        val events = storage.query(filter)
        return buildString {
            appendLine("Timestamp,Type,Name,DurationMs,Source,Parameters")
            events.forEach { event ->
                append(formatTimestamp(event.timestamp))
                append(",")
                append(event.eventType.name)
                append(",")
                append(escapeCsv(event.name))
                append(",")
                append(event.durationMs?.toString() ?: "")
                append(",")
                append(escapeCsv(event.source))
                append(",")
                append(escapeCsv(event.parameters.entries.joinToString("; ") { "${it.key}=${it.value}" }))
                appendLine()
            }
        }
    }

    /**
     * Export discrete analytics/user events to Markdown format.
     */
    suspend fun exportEventsAsMarkdown(
        storage: EventLogStorage,
        filter: EventFilter = EventFilter.NONE,
    ): String {
        val events = storage.query(filter)
        return buildString {
            appendLine("# Spectra Events & User Analytics Report")
            appendLine("- **Generated**: ${formatTimestamp(SpectraTime.now())}")
            appendLine("- **Total Events**: ${events.size}")
            appendLine()
            appendLine("| Time | Type | Name | Duration | Source |")
            appendLine("|---|---|---|---|---|")
            events.forEach { event ->
                val duration = event.durationMs?.let { "${it}ms" } ?: "-"
                appendLine(
                    "| ${formatTimestamp(event.timestamp)} | `${event.eventType.name}` | " +
                        "`${escapeMarkdownCell(event.name)}` | $duration | `${escapeMarkdownCell(event.source)}` |",
                )
            }
        }
    }

    /**
     * Export unified full debug bundle combining Application Logs, Network Requests, and Events into a single JSON document.
     */
    suspend fun exportFullBundleAsJson(
        logStorage: LogStorage,
        networkStorage: NetworkLogStorage,
        eventStorage: EventLogStorage,
        deviceInfo: DeviceInfo? = null,
    ): String {
        val logsJson = exportLogsAsJson(logStorage)
        val netJson = exportNetworkLogsAsJson(networkStorage)
        val eventsJson = exportEventsAsJson(eventStorage)

        return buildString {
            appendLine("{")
            appendLine("  \"bundleTimestamp\": \"${SpectraTime.now()}\",")
            if (deviceInfo != null) {
                appendLine("  \"deviceInfo\": {")
                appendLine("    \"deviceId\": \"${escapeJson(deviceInfo.deviceId)}\",")
                appendLine("    \"deviceName\": \"${escapeJson(deviceInfo.deviceName)}\",")
                appendLine("    \"os\": \"${escapeJson(deviceInfo.os)}\",")
                appendLine("    \"osVersion\": \"${escapeJson(deviceInfo.osVersion)}\",")
                appendLine("    \"appVersion\": \"${escapeJson(deviceInfo.appVersion)}\"")
                appendLine("  },")
            }
            appendLine("  \"applicationLogs\": $logsJson,")
            appendLine("  \"networkTelemetry\": $netJson,")
            appendLine("  \"userEvents\": $eventsJson")
            appendLine("}")
        }
    }

    /**
     * Export unified full debug bundle combining Application Logs, Network Requests, and Events into a Markdown summary.
     */
    suspend fun exportFullBundleAsMarkdown(
        logStorage: LogStorage,
        networkStorage: NetworkLogStorage,
        eventStorage: EventLogStorage,
        deviceInfo: DeviceInfo? = null,
    ): String {
        val logs = logStorage.query()
        val network = networkStorage.query()
        val events = eventStorage.query()

        return buildString {
            appendLine("# Spectra Complete Telemetry Debug Report")
            appendLine("- **Report Timestamp**: ${formatTimestamp(SpectraTime.now())}")
            if (deviceInfo != null) {
                appendLine("- **Device**: ${deviceInfo.deviceName} (${deviceInfo.os} ${deviceInfo.osVersion})")
                appendLine("- **App Version**: ${deviceInfo.appVersion}")
            }
            appendLine()
            appendLine("## Telemetry Summary")
            appendLine("| Category | Count | Status |")
            appendLine("|---|---|---|")
            appendLine("| **Application Logs** | ${logs.size} | ${logs.count { it.level.name == "ERROR" }} errors |")
            appendLine("| **Network Requests** | ${network.size} | ${network.count { it.isFailed }} failed |")
            appendLine("| **User Events** | ${events.size} | ${events.count { it.eventType.name == "SCREEN_VIEW" }} screen views |")
            appendLine()
            appendLine("---")
            appendLine()
            append(exportLogsAsMarkdown(logStorage))
            appendLine()
            appendLine("---")
            appendLine()
            append(exportNetworkLogsAsMarkdown(networkStorage))
            appendLine()
            appendLine("---")
            appendLine()
            append(exportEventsAsMarkdown(eventStorage))
        }
    }

    /**
     * Export network logs to Markdown format.
     */
    suspend fun exportNetworkLogsAsMarkdown(
        storage: NetworkLogStorage,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
    ): String {
        val logs = storage.query(filter)
        return buildString {
            appendLine("# Spectra Network Telemetry Report")
            appendLine("- **Generated**: ${formatTimestamp(SpectraTime.now())}")
            appendLine("- **Total Requests**: ${logs.size}")
            appendLine()
            appendLine("| Time | Method | Status | Duration | URL |")
            appendLine("|---|---|---|---|---|")
            logs.forEach { log ->
                val status = log.responseCode?.toString() ?: "ERROR"
                appendLine(
                    "| ${formatTimestamp(log.timestamp)} | `${log.method}` | `$status` | " +
                        "${log.duration}ms | `${escapeMarkdownCell(log.url)}` |",
                )
            }
        }
    }

    private fun formatHarHeaders(log: NetworkLogEntry): Triple<String, String, String> {
        val queryParts = log.url.substringAfter('?', "").takeIf { it.isNotEmpty() }?.split('&').orEmpty()
        val queryJson =
            if (queryParts.isEmpty()) {
                "[]"
            } else {
                queryParts.joinToString(
                    prefix = "[\n            ",
                    separator = ",\n            ",
                    postfix = "\n          ]",
                ) { part ->
                    val name = escapeJson(part.substringBefore('='))
                    val value = escapeJson(part.substringAfter('=', ""))
                    "{\"name\": \"$name\", \"value\": \"$value\"}"
                }
            }

        val reqHeadersJson =
            if (log.requestHeaders.isEmpty()) {
                "[]"
            } else {
                log.requestHeaders.entries.joinToString(
                    prefix = "[\n            ",
                    separator = ",\n            ",
                    postfix = "\n          ]",
                ) { (k, v) ->
                    "{\"name\": \"${escapeJson(k)}\", \"value\": \"${escapeJson(v)}\"}"
                }
            }

        val resHeadersJson =
            if (log.responseHeaders.isEmpty()) {
                "[]"
            } else {
                log.responseHeaders.entries.joinToString(
                    prefix = "[\n            ",
                    separator = ",\n            ",
                    postfix = "\n          ]",
                ) { (k, v) ->
                    "{\"name\": \"${escapeJson(k)}\", \"value\": \"${escapeJson(v)}\"}"
                }
            }

        return Triple(queryJson, reqHeadersJson, resHeadersJson)
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

    private fun escapeMarkdownCell(str: String): String =
        str
            .replace("|", "\\|")
            .replace("\n", " ")
            .replace("\r", "")
}
