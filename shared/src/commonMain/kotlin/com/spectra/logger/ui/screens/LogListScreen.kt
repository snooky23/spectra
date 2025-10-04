package com.spectra.logger.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.ui.components.LogDetailDialog
import com.spectra.logger.ui.components.LogEntryItem
import kotlinx.coroutines.flow.catch

/**
 * Screen displaying a list of log entries.
 *
 * @param storage Log storage to query
 * @param filter Optional filter for logs
 * @param modifier Modifier for the screen
 * @param onLogClick Optional click handler for log entries
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionName")
fun LogListScreen(
    storage: LogStorage,
    filter: LogFilter = LogFilter.NONE,
    modifier: Modifier = Modifier,
    onLogClick: ((LogEntry) -> Unit)? = null,
) {
    var logs by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }

    // Observe logs from storage
    val newLogs by
        storage.observe(filter)
            .catch { e -> error = e.message }
            .collectAsState(initial = null)

    // Load initial logs
    LaunchedEffect(filter) {
        try {
            logs = storage.query(filter)
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    // Add new logs as they arrive
    LaunchedEffect(newLogs) {
        newLogs?.let { newLog ->
            logs = listOf(newLog) + logs
        }
    }

    // Show detail dialog if a log is selected
    selectedLog?.let { log ->
        LogDetailDialog(
            entry = log,
            onDismiss = { selectedLog = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs (${logs.size})") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                    )
                }
                logs.isEmpty() -> {
                    Text(
                        text = "No logs to display",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(logs, key = { it.id }) { log ->
                            LogEntryItem(
                                entry = log,
                                onClick = {
                                    selectedLog = log
                                    onLogClick?.invoke(log)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
