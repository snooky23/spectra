package com.spectra.logger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.model.NetworkLogFilter
import com.spectra.logger.domain.storage.NetworkLogStorage
import com.spectra.logger.ui.components.NetworkLogDetailDialog
import com.spectra.logger.ui.components.NetworkLogItem
import com.spectra.logger.ui.theme.LogColors
import kotlinx.coroutines.flow.catch

/**
 * Screen displaying a list of network log entries.
 *
 * @param storage Network log storage to query
 * @param filter Optional filter for network logs
 * @param modifier Modifier for the screen
 * @param onLogClick Optional click handler for network log entries
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NetworkLogScreen(
    storage: NetworkLogStorage,
    filter: NetworkLogFilter = NetworkLogFilter.NONE,
    modifier: Modifier = Modifier,
    onLogClick: ((NetworkLogEntry) -> Unit)? = null,
) {
    var logs by remember { mutableStateOf<List<NetworkLogEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMethods by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedStatusRanges by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }

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

    // Filter logs based on search and filters (minimum 2 characters for search)
    val filteredLogs =
        logs.filter { log ->
            val matchesSearch =
                searchQuery.length < 2 ||
                    log.url.contains(searchQuery, ignoreCase = true) ||
                    log.method.contains(searchQuery, ignoreCase = true)

            val matchesMethod =
                selectedMethods.isEmpty() || selectedMethods.contains(log.method)

            val matchesStatusRange =
                selectedStatusRanges.isEmpty() ||
                    selectedStatusRanges.any { range ->
                        log.responseCode?.let { status ->
                            when (range) {
                                "2xx" -> status in 200..299
                                "3xx" -> status in 300..399
                                "4xx" -> status in 400..499
                                "5xx" -> status in 500..599
                                else -> false
                            }
                        } ?: false
                    }

            matchesSearch && matchesMethod && matchesStatusRange
        }

    val httpMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
    val statusRanges = listOf("2xx", "3xx", "4xx", "5xx")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Network Logs (${filteredLogs.size}/${logs.size})",
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
                placeholder = { Text("Search by URL or method (min 2 chars)...") },
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

            // HTTP Method Filters
            if (httpMethods.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    httpMethods.forEach { method ->
                        FilterChip(
                            selected = selectedMethods.contains(method),
                            onClick = {
                                selectedMethods =
                                    if (selectedMethods.contains(method)) {
                                        selectedMethods - method
                                    } else {
                                        selectedMethods + method
                                    }
                            },
                            label = { Text(method) },
                        )
                    }
                }
            }

            // Status Range Filters
            if (statusRanges.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    statusRanges.forEach { range ->
                        val statusColor =
                            when (range) {
                                "2xx" -> LogColors.NetworkStatus.Success
                                "3xx" -> LogColors.NetworkStatus.Redirect
                                "4xx" -> LogColors.NetworkStatus.ClientError
                                "5xx" -> LogColors.NetworkStatus.ServerError
                                else -> LogColors.NetworkStatus.Unknown
                            }

                        FilterChip(
                            selected = selectedStatusRanges.contains(range),
                            onClick = {
                                selectedStatusRanges =
                                    if (selectedStatusRanges.contains(range)) {
                                        selectedStatusRanges - range
                                    } else {
                                        selectedStatusRanges + range
                                    }
                            },
                            label = { Text(range) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = statusColor.copy(alpha = 0.2f),
                                    selectedLabelColor = statusColor,
                                    selectedLeadingIconColor = statusColor,
                                ),
                        )
                    }
                }
            }

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
                    logs.isEmpty() -> {
                        Text(
                            text = "No network logs to display",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
                        )
                    }
                    filteredLogs.isEmpty() -> {
                        Text(
                            text = "No logs match the current filters",
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
                                NetworkLogItem(
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

    // Show detail dialog when a log is selected
    selectedLog?.let { log ->
        NetworkLogDetailDialog(
            entry = log,
            onDismiss = { selectedLog = null },
        )
    }
}
