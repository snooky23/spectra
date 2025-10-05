package com.spectra.logger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.ui.components.LogDetailDialog
import com.spectra.logger.ui.components.LogEntryItem
import com.spectra.logger.ui.theme.LogColors
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedLogLevels by remember { mutableStateOf(setOf<LogLevel>()) }

    // Filter logs based on search query and log levels
    val filteredLogs =
        remember(logs, searchQuery, selectedLogLevels) {
            var filtered = logs

            // Filter by log levels
            if (selectedLogLevels.isNotEmpty()) {
                filtered =
                    filtered.filter { log ->
                        selectedLogLevels.contains(log.level)
                    }
            }

            // Filter by search query (minimum 2 characters)
            if (searchQuery.length >= 2) {
                filtered =
                    filtered.filter { log ->
                        log.message.contains(searchQuery, ignoreCase = true) ||
                            log.tag.contains(searchQuery, ignoreCase = true) ||
                            log.level.name.contains(searchQuery, ignoreCase = true) ||
                            log.throwable?.contains(searchQuery, ignoreCase = true) == true ||
                            log.metadata.any { (key, value) ->
                                key.contains(searchQuery, ignoreCase = true) ||
                                    value.contains(searchQuery, ignoreCase = true)
                            }
                    }
            }

            filtered
        }

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
                title = {
                    Text(
                        if (searchQuery.length >= 2 || selectedLogLevels.isNotEmpty()) {
                            "Logs (${filteredLogs.size}/${logs.size})"
                        } else {
                            "Logs (${logs.size})"
                        },
                    )
                },
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
                    .padding(paddingValues),
        ) {
            // Search field (minimum 2 characters to filter)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search logs (min 2 chars)...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
            )

            // Log level filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(LogLevel.entries) { level ->
                    FilterChip(
                        selected = selectedLogLevels.contains(level),
                        onClick = {
                            selectedLogLevels =
                                if (selectedLogLevels.contains(level)) {
                                    selectedLogLevels - level
                                } else {
                                    selectedLogLevels + level
                                }
                        },
                        label = { Text(level.name) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LogColors.getColor(level).copy(alpha = 0.2f),
                                selectedLabelColor = LogColors.getColor(level),
                                selectedLeadingIconColor = LogColors.getColor(level),
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Box(
                modifier = Modifier.fillMaxSize(),
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
                    filteredLogs.isEmpty() -> {
                        Text(
                            text = if (logs.isEmpty()) "No logs to display" else "No matching logs",
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
                            items(filteredLogs, key = { it.id }) { log ->
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
}
