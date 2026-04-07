package com.spectra.logger.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.ui.compose.components.DetailSection
import com.spectra.logger.ui.util.PlatformUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Detailed view of a network request/response.
 * Can be used as a standalone pane or inside a sheet.
 */
@Composable
fun NetworkDetailPane(
    log: NetworkLogEntry,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Request") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Response") })
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            when (selectedTab) {
                0 -> OverviewTab(log)
                1 -> RequestTab(log)
                2 -> ResponseTab(log)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val text =
                            buildString {
                                appendLine("URL: ${log.url}")
                                appendLine("Method: ${log.method}")
                                appendLine("Status: ${log.responseCode}")
                                appendLine()
                                appendLine("Request Headers:")
                                appendLine(log.requestHeaders)
                                appendLine()
                                appendLine("Request Body:")
                                appendLine(log.requestBody ?: "")
                                appendLine()
                                appendLine("Response Headers:")
                                appendLine(log.responseHeaders)
                                appendLine()
                                appendLine("Response Body:")
                                appendLine(log.responseBody ?: "")
                            }
                        PlatformUtils.shareText(text, "Network Request")
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share All")
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(log: NetworkLogEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailSection(title = "General") {
            DetailItem(label = "URL", value = log.url)
            DetailItem(label = "Method", value = log.method)
            DetailItem(label = "Status", value = log.responseCode?.toString() ?: "Error")
            DetailItem(label = "Duration", value = "${log.duration} ms")
            DetailItem(label = "Timestamp", value = formatFullTime(log.timestamp))
            DetailItem(label = "Source", value = log.source)
        }

        if (log.error != null) {
            DetailSection(title = "Error", titleColor = MaterialTheme.colorScheme.error) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                ) {
                    Text(
                        text = log.error!!,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestTab(log: NetworkLogEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailSection(title = "Request Headers") {
            if (log.requestHeaders.isEmpty()) {
                Text("No headers", style = MaterialTheme.typography.bodySmall)
            } else {
                log.requestHeaders.forEach { (key, value) ->
                    HeaderRow(key = key, value = value)
                }
            }
        }

        DetailSection(title = "Request Body") {
            BodyContent(log.requestBody)
        }
    }
}

@Composable
private fun ResponseTab(log: NetworkLogEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailSection(title = "Response Headers") {
            if (log.responseHeaders.isEmpty()) {
                Text("No headers", style = MaterialTheme.typography.bodySmall)
            } else {
                log.responseHeaders.forEach { (key, value) ->
                    HeaderRow(key = key, value = value)
                }
            }
        }

        DetailSection(title = "Response Body") {
            BodyContent(log.responseBody)
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun HeaderRow(
    key: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$key:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun BodyContent(body: String?) {
    if (body.isNullOrEmpty()) {
        Text("Empty body", style = MaterialTheme.typography.bodySmall)
    } else {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            Text(
                text = body,
                modifier = Modifier.padding(12.dp),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                    ),
            )
        }
    }
}

private fun formatFullTime(timestamp: kotlinx.datetime.Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.hour.toString().padStart(
        2,
        '0',
    )}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
}
