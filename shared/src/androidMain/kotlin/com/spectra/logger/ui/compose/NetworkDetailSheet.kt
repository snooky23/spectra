package com.spectra.logger.ui.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.spectra.logger.network.NetworkLogEntry
import com.spectra.logger.ui.compose.components.DetailSection
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Network log detail sheet showing full request/response information
 * Matches iOS NetworkLogDetailView design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailSheet(
    log: NetworkLogEntry,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MethodBadge(method = log.method)
                    log.statusCode?.let { StatusCodeBadge(statusCode = it) }
                }
                
                Spacer(modifier = Modifier.width(48.dp))
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // URL section
                DetailSection(title = "URL") {
                    Text(
                        text = log.url,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Timestamp
                DetailSection(title = "Timestamp") {
                    Text(text = formatFullTime(log.timestamp))
                }

                // Duration (if available)
                log.duration?.let { duration ->
                    DetailSection(title = "Duration") {
                        Text(text = "${duration}ms")
                    }
                }

                // Request Headers
                if (log.requestHeaders.isNotEmpty()) {
                    DetailSection(title = "Request Headers") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            log.requestHeaders.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Request Body
                log.requestBody?.let { body ->
                    if (body.isNotEmpty()) {
                        DetailSection(title = "Request Body") {
                            Box(
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = body,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Response Headers
                if (log.responseHeaders.isNotEmpty()) {
                    DetailSection(title = "Response Headers") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            log.responseHeaders.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Response Body
                log.responseBody?.let { body ->
                    if (body.isNotEmpty()) {
                        DetailSection(title = "Response Body") {
                            Box(
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = body,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Error
                log.error?.let { error ->
                    DetailSection(title = "Error") {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private fun formatFullTime(timestamp: kotlinx.datetime.Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    return String.format(
        "%04d-%02d-%02d %02d:%02d:%02d",
        localDateTime.year,
        localDateTime.monthNumber,
        localDateTime.dayOfMonth,
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.second
    )
}
