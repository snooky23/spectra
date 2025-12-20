package com.spectra.logger.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.ui.compose.components.ActiveFilterBadge
import com.spectra.logger.ui.compose.components.EmptyState
import com.spectra.logger.ui.compose.components.SearchBar
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Network logs screen displaying HTTP request/response logs.
 * Matches iOS NetworkLogsView design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogsScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkLogsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Network") },
                actions = {
                    // Filter button with badge
                    BadgedBox(
                        badge = {
                            if (uiState.totalActiveFilterCount > 0) {
                                Badge { Text("${uiState.totalActiveFilterCount}") }
                            }
                        },
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }

                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            onClick = {
                                viewModel.loadLogs()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Logs") },
                            onClick = {
                                viewModel.clearLogs()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchText,
                onQueryChange = viewModel::onSearchTextChanged,
                placeholder = "Search...",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Active filter badges (when filters are active)
            if (uiState.hasActiveFilters) {
                Row(
                    modifier =
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Method filters
                    uiState.selectedMethods.forEach { method ->
                        ActiveFilterBadge(
                            label = method,
                            onRemove = { viewModel.removeMethodFilter(method) },
                        )
                    }
                    // Status range filters
                    uiState.selectedStatusRanges.forEach { range ->
                        ActiveFilterBadge(
                            label = range,
                            onRemove = { viewModel.removeStatusRangeFilter(range) },
                        )
                    }
                    // Host filter
                    if (uiState.advancedFilter.hostPattern.isNotEmpty()) {
                        ActiveFilterBadge(
                            label = "Host: ${uiState.advancedFilter.hostPattern}",
                            onRemove = { viewModel.clearHostFilter() },
                        )
                    }
                    // Time range filter
                    if (uiState.advancedFilter.fromTimestamp != null ||
                        uiState.advancedFilter.toTimestamp != null
                    ) {
                        ActiveFilterBadge(
                            label = "Time Range",
                            onRemove = { viewModel.clearTimeRangeFilter() },
                        )
                    }
                    // Response time filter
                    uiState.advancedFilter.responseTimeThreshold?.let { threshold ->
                        ActiveFilterBadge(
                            label = threshold.label,
                            onRemove = { viewModel.clearResponseTimeFilter() },
                        )
                    }
                    // Failed only filter
                    if (uiState.advancedFilter.showOnlyFailed) {
                        ActiveFilterBadge(
                            label = "Errors Only",
                            onRemove = { viewModel.clearFailedOnlyFilter() },
                        )
                    }
                }
            }

            HorizontalDivider()

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.filteredLogs.isEmpty() -> {
                    EmptyState(
                        icon = if (uiState.logs.isEmpty()) Icons.Default.Inbox else Icons.Default.Search,
                        message = if (uiState.logs.isEmpty()) "No network logs to display" else "No matching logs",
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(uiState.filteredLogs, key = { it.id }) { log ->
                            NetworkLogRow(
                                log = log,
                                onClick = { selectedLog = log },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // Network detail sheet
    selectedLog?.let { log ->
        NetworkDetailSheet(
            log = log,
            onDismiss = { selectedLog = null },
        )
    }

    // Network filter sheet
    if (showFilterSheet) {
        NetworkFilterSheet(
            filter = uiState.advancedFilter,
            onFilterChange = { viewModel.updateAdvancedFilter(it) },
            onDismiss = { showFilterSheet = false },
        )
    }
}

/**
 * Network log row component
 */
@Composable
fun NetworkLogRow(
    log: NetworkLogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Method badge
                MethodBadge(method = log.method)

                // Status code badge
                log.responseCode?.let { code ->
                    StatusCodeBadge(statusCode = code)
                }
            }

            // Timestamp
            Text(
                text = formatShortTime(log.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // URL
        Text(
            text = log.url,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * HTTP method badge
 */
@Composable
fun MethodBadge(method: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF2196F3).copy(alpha = 0.2f),
    ) {
        Text(
            text = method,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2196F3),
        )
    }
}

/**
 * HTTP status code badge
 */
@Composable
fun StatusCodeBadge(statusCode: Int) {
    val color = colorForStatusCode(statusCode)

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f),
    ) {
        Text(
            text = statusCode.toString(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

fun colorForStatusCode(status: Int): Color {
    return when (status) {
        in 200..299 -> Color(0xFF4CAF50) // Green
        in 300..399 -> Color(0xFF2196F3) // Blue
        in 400..499 -> Color(0xFFFF9800) // Orange
        in 500..599 -> Color(0xFFF44336) // Red
        else -> Color.Gray
    }
}

fun colorForStatusRange(range: String): Color {
    return when (range) {
        "2xx" -> Color(0xFF4CAF50)
        "3xx" -> Color(0xFF2196F3)
        "4xx" -> Color(0xFFFF9800)
        "5xx" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

private fun formatShortTime(timestamp: kotlinx.datetime.Instant): String {
    val localDateTime = timestamp.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return String.format(
        "%02d:%02d:%02d",
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.second,
    )
}
