package com.spectra.logger.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.domain.storage.NetworkLogStorage
import kotlinx.coroutines.launch

/**
 * Settings screen for configuring logger behavior.
 *
 * @param logStorage Log storage instance
 * @param networkLogStorage Network log storage instance
 * @param currentMinLevel Current minimum log level
 * @param onMinLevelChange Callback when minimum log level changes
 * @param onExportLogs Callback to export logs
 * @param modifier Modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    logStorage: LogStorage,
    networkLogStorage: NetworkLogStorage,
    currentMinLevel: LogLevel = LogLevel.VERBOSE,
    onMinLevelChange: (LogLevel) -> Unit = {},
    onExportLogs: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var showClearLogsDialog by remember { mutableStateOf(false) }
    var showClearNetworkDialog by remember { mutableStateOf(false) }
    var logCount by remember { mutableStateOf(0) }
    var networkLogCount by remember { mutableStateOf(0) }

    // Load counts
    scope.launch {
        logCount = logStorage.count()
        networkLogCount = networkLogStorage.count()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            // Log Level Section
            SettingSection(title = "Log Level") {
                Text(
                    text = "Set minimum log level to display",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))

                val logLevels = LogLevel.entries
                var selectedIndex by remember { mutableStateOf(logLevels.indexOf(currentMinLevel)) }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    logLevels.forEachIndexed { index, level ->
                        SegmentedButton(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                onMinLevelChange(level)
                            },
                            shape =
                                SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = logLevels.size,
                                ),
                        ) {
                            Text(level.name.take(1))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Storage Section
            SettingSection(title = "Storage") {
                // Log storage info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Application Logs",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "$logCount logs stored",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(
                        onClick = { showClearLogsDialog = true },
                        enabled = logCount > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear logs",
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Clear")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Network log storage info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Network Logs",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "$networkLogCount logs stored",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(
                        onClick = { showClearNetworkDialog = true },
                        enabled = networkLogCount > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear network logs",
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Export Section
            SettingSection(title = "Export") {
                Text(
                    text = "Export all logs to share with developers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onExportLogs,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Export All Logs")
                }
            }
        }
    }

    // Clear logs confirmation dialog
    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            title = { Text("Clear Application Logs?") },
            text = {
                Text("This will permanently delete all $logCount application logs. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            logStorage.clear()
                            logCount = 0
                            showClearLogsDialog = false
                        }
                    },
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Clear network logs confirmation dialog
    if (showClearNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showClearNetworkDialog = false },
            title = { Text("Clear Network Logs?") },
            text = {
                Text("This will permanently delete all $networkLogCount network logs. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            networkLogStorage.clear()
                            networkLogCount = 0
                            showClearNetworkDialog = false
                        }
                    },
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearNetworkDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
