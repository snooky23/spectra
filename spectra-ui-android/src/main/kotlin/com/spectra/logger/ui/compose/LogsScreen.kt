package com.spectra.logger.ui.compose

import androidx.compose.foundation.background
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
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.ui.compose.components.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Logs screen displaying application logs with filtering.
 * Matches iOS LogsView design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    viewModel: LogsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                actions = {
                    // Filter button with badge
                    BadgedBox(
                        badge = {
                            if (uiState.totalActiveFilterCount > 0) {
                                Badge { Text("${uiState.totalActiveFilterCount}") }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                    
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            onClick = {
                                viewModel.loadLogs()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Logs") },
                            onClick = {
                                viewModel.clearLogs()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchText,
                onQueryChange = viewModel::onSearchTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Active filter badges (including levels)
            if (uiState.hasAnyActiveFilters) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Level badges
                    uiState.selectedLevels.sortedBy { it.ordinal }.forEach { level ->
                        ActiveFilterBadge(
                            label = level.name,
                            onRemove = { viewModel.toggleLevel(level) }
                        )
                    }
                    // Tag badges
                    uiState.selectedTags.forEach { tag ->
                        ActiveFilterBadge(
                            label = "Tag: $tag",
                            onRemove = { viewModel.removeTagFilter(tag) }
                        )
                    }
                    if (uiState.hasTimeRangeFilter) {
                        ActiveFilterBadge(
                            label = "Time Range",
                            onRemove = { viewModel.clearTimeRangeFilter() }
                        )
                    }
                    if (uiState.hasErrorOnly) {
                        ActiveFilterBadge(
                            label = "Errors Only",
                            onRemove = { viewModel.clearHasErrorFilter() }
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
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.filteredLogs.isEmpty() -> {
                    EmptyState(
                        icon = if (uiState.logs.isEmpty()) Icons.Default.Inbox else Icons.Default.Search,
                        message = if (uiState.logs.isEmpty()) "No logs to display" else "No matching logs"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredLogs, key = { it.id }) { log ->
                            LogRow(
                                log = log,
                                onClick = { selectedLog = log }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        LogsFilterSheet(
            filter = uiState.advancedFilter,
            selectedLevels = uiState.selectedLevels,
            availableTags = uiState.availableTags,
            onFilterChange = viewModel::updateFilter,
            onLevelsChange = viewModel::updateLevels,
            onDismiss = { showFilterSheet = false }
        )
    }

    // Log detail sheet
    selectedLog?.let { log ->
        LogDetailSheet(
            log = log,
            onDismiss = { selectedLog = null }
        )
    }
    
    // Share dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Logs") },
            text = { Text("Choose which logs to share") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.shareLogs(uiState.filteredLogs)
                        showShareDialog = false
                    }
                ) {
                    Text("Share Filtered (${uiState.filteredLogs.size} items)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.shareLogs(uiState.logs)
                        showShareDialog = false
                    }
                ) {
                    Text("Share All (${uiState.logs.size} items)")
                }
            }
        )
    }
}

/**
 * Single log row component
 */
@Composable
fun LogRow(
    log: LogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge
                LogLevelBadge(level = log.level)
                
                // Tag
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Timestamp
            Text(
                text = formatShortTime(log.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Message
        Text(
            text = log.message,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Error indicator
        if (log.throwable != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFF9800)
                )
                Text(
                    text = "Has Error",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

/**
 * Log level badge with appropriate color
 */
@Composable
fun LogLevelBadge(level: LogLevel) {
    val color = colorForLogLevel(level)
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = level.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Get color for log level
 */
fun colorForLogLevel(level: LogLevel): Color {
    return when (level) {
        LogLevel.VERBOSE -> Color.Gray
        LogLevel.DEBUG -> Color(0xFF2196F3) // Blue
        LogLevel.INFO -> Color(0xFF4CAF50) // Green
        LogLevel.WARNING -> Color(0xFFFF9800) // Orange
        LogLevel.ERROR -> Color(0xFFF44336) // Red
        LogLevel.FATAL -> Color(0xFF9C27B0) // Purple
    }
}

private fun formatShortTime(timestamp: kotlinx.datetime.Instant): String {
    val localDateTime = timestamp.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return String.format(
        "%02d:%02d:%02d",
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.second
    )
}
