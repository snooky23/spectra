package com.spectra.logger.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.ui.compose.components.SettingsCard

/**
 * Settings screen for configuration and log management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onDismiss: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearLogsDialog by remember { mutableStateOf(false) }
    var showClearNetworkDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Appearance Section
            SettingsSection(title = "APPEARANCE") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppearanceMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = uiState.appearanceMode == mode,
                            onClick = { viewModel.setAppearanceMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AppearanceMode.entries.size),
                        ) {
                            Text(mode.label)
                        }
                    }
                }
            }

            // Storage Section
            SettingsSection(title = "STORAGE") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Application Logs
                    StorageItem(
                        label = "Application Logs",
                        count = uiState.applicationLogCount,
                        onClear = { showClearLogsDialog = true },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Network Logs
                    StorageItem(
                        label = "Network Logs",
                        count = uiState.networkLogCount,
                        onClear = { showClearNetworkDialog = true },
                    )
                }
            }

            // Configuration Section
            SettingsSection(title = "CONFIGURATION") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Network Logging Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Network Logging", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Automatically capture HTTP requests",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = uiState.isNetworkLoggingEnabled,
                            onCheckedChange = viewModel::toggleNetworkLogging,
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // File Persistence Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("File Persistence", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Save logs to disk across app restarts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = uiState.isFilePersistenceEnabled,
                            onCheckedChange = viewModel::toggleFilePersistence,
                        )
                    }
                }
            }

            // Export Section
            SettingsSection(title = "EXPORT") {
                OutlinedButton(
                    onClick = { viewModel.exportAllLogs() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export All Logs")
                }
            }

            // Info Section
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Spectra Logger v${uiState.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // Dialogs
    if (showClearLogsDialog) {
        ClearLogsDialog(
            title = "Clear Application Logs",
            onConfirm = {
                viewModel.clearApplicationLogs()
                showClearLogsDialog = false
            },
            onDismiss = { showClearLogsDialog = false },
        )
    }

    if (showClearNetworkDialog) {
        ClearLogsDialog(
            title = "Clear Network Logs",
            onConfirm = {
                viewModel.clearNetworkLogs()
                showClearNetworkDialog = false
            },
            onDismiss = { showClearNetworkDialog = false },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun StorageItem(
    label: String,
    count: Int,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "$count entries stored",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onClear, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear")
        }
    }
}

@Composable
private fun ClearLogsDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text("This action cannot be undone. Are you sure?") },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Clear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
