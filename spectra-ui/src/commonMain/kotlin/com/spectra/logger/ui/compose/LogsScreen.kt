package com.spectra.logger.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.ui.compose.components.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Logs screen displaying application logs with simplified filtering and detail views.
 * Bypasses NavigableListDetailPaneScaffold to ensure multiplatform compilation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    viewModel: LogsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedLog == null) {
            LogsListContent(
                uiState = uiState,
                onLogClick = { log ->
                    selectedLog = log
                },
                onShowFilter = { showFilterSheet = true },
                onShowShare = { showShareBottomSheet = true },
                onRefresh = viewModel::loadLogs,
                onClearLogs = viewModel::clearLogs,
                onSearchChange = viewModel::onSearchTextChanged,
                onToggleLevel = viewModel::toggleLevel,
                onRemoveTag = viewModel::removeTagFilter,
                onClearTimeRange = viewModel::clearTimeRangeFilter,
                onClearHasError = viewModel::clearHasErrorFilter,
            )
        } else {
            LogDetailContent(
                log = selectedLog!!,
                onBack = {
                    selectedLog = null
                },
            )
        }

        // Filter bottom sheet
        if (showFilterSheet) {
            LogsFilterSheet(
                filter = uiState.advancedFilter,
                selectedLevels = uiState.selectedLevels,
                availableTags = uiState.availableTags,
                onFilterChange = viewModel::updateFilter,
                onLevelsChange = viewModel::updateLevels,
                onDismiss = { showFilterSheet = false },
            )
        }

        // Share bottom sheet
        if (showShareBottomSheet) {
            ShareBottomSheet(
                filteredCount = uiState.filteredLogs.size,
                totalCount = uiState.logs.size,
                onShareFiltered = { viewModel.shareLogs(uiState.filteredLogs) },
                onShareAll = { viewModel.shareLogs(uiState.logs) },
                onDismiss = { showShareBottomSheet = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsListContent(
    uiState: LogsUiState,
    onLogClick: (LogEntry) -> Unit,
    onShowFilter: () -> Unit,
    onShowShare: () -> Unit,
    onRefresh: () -> Unit,
    onClearLogs: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleLevel: (LogLevel) -> Unit,
    onRemoveTag: (String) -> Unit,
    onClearTimeRange: () -> Unit,
    onClearHasError: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (uiState.totalActiveFilterCount > 0) {
                                Badge { Text("${uiState.totalActiveFilterCount}") }
                            }
                        },
                    ) {
                        IconButton(onClick = onShowFilter) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }

                    IconButton(onClick = onShowShare) {
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
                                onRefresh()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Logs") },
                            onClick = {
                                onClearLogs()
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            SearchBar(
                query = uiState.searchText,
                onQueryChange = onSearchChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (uiState.hasAnyActiveFilters) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.selectedLevels.sortedBy { it.ordinal }.forEach { level ->
                        ActiveFilterBadge(label = level.name, onRemove = { onToggleLevel(level) })
                    }
                    uiState.selectedTags.forEach { tag ->
                        ActiveFilterBadge(label = "Tag: $tag", onRemove = { onRemoveTag(tag) })
                    }
                    if (uiState.hasTimeRangeFilter) {
                        ActiveFilterBadge(label = "Time Range", onRemove = onClearTimeRange)
                    }
                    if (uiState.hasErrorOnly) {
                        ActiveFilterBadge(label = "Errors Only", onRemove = onClearHasError)
                    }
                }
            }

            HorizontalDivider()

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.filteredLogs.isEmpty() -> {
                    EmptyState(
                        icon = if (uiState.logs.isEmpty()) Icons.Default.Inbox else Icons.Default.Search,
                        message = if (uiState.logs.isEmpty()) "No logs to display" else "No matching logs",
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.filteredLogs, key = { it.id }) { log ->
                            LogRow(log = log, onClick = { onLogClick(log) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogDetailContent(
    log: LogEntry,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LogDetailPane(log = log)
        }
    }
}

@Composable
fun LogRow(
    log: LogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
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
                LogLevelBadge(level = log.level)
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatShortTime(log.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = log.message,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (log.throwable != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFF9800),
                )
                Text(
                    text = "Has Error",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF9800),
                )
            }
        }
    }
}

private fun formatShortTime(timestamp: kotlinx.datetime.Instant): String {
    val localDateTime = timestamp.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')
    return "$hour:$minute:$second"
}
