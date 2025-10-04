package com.spectra.logger.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.NetworkLogEntry

/**
 * Dialog showing detailed information about a network log entry.
 *
 * @param entry The network log entry to display
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun NetworkLogDetailDialog(
    entry: NetworkLogEntry,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("${entry.method} ${entry.responseCode ?: "ERR"}")
                Text(
                    text = entry.url,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tabs for different sections
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Overview") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Request") },
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Response") },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on selected tab
                when (selectedTab) {
                    0 -> OverviewTab(entry)
                    1 -> RequestTab(entry)
                    2 -> ResponseTab(entry)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun OverviewTab(entry: NetworkLogEntry) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        DetailRow("Method", entry.method)
        DetailRow("URL", entry.url)
        DetailRow("Status Code", entry.responseCode?.toString() ?: "N/A")
        DetailRow("Duration", "${entry.duration}ms")
        if (entry.error != null) {
            DetailRow("Error", entry.error, isError = true)
        }
    }
}

@Composable
private fun RequestTab(entry: NetworkLogEntry) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (entry.requestHeaders.isNotEmpty()) {
            SectionHeader("Headers")
            entry.requestHeaders.forEach { (key, value) ->
                DetailRow(key, value)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (entry.requestBody != null) {
            SectionHeader("Body")
            Text(
                text = entry.requestBody,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ResponseTab(entry: NetworkLogEntry) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (entry.responseHeaders.isNotEmpty()) {
            SectionHeader("Headers")
            entry.responseHeaders.forEach { (key, value) ->
                DetailRow(key, value)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (entry.responseBody != null) {
            SectionHeader("Body")
            Text(
                text = entry.responseBody,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isError: Boolean = false,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.3f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color =
                if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            modifier = Modifier.weight(0.7f),
        )
    }
}
