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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.ui.compose.components.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Network logs screen displaying HTTP request/response logs with adaptive layouts.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogsScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkLogsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showShareBottomSheet by remember { mutableStateOf(false) }

    NavigableListDetailPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        listPane = {
            AnimatedPane {
                NetworkListContent(
                    uiState = uiState,
                    onLogClick = { log ->
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, log.id)
                        }
                    },
                    onShowFilter = { showFilterSheet = true },
                    onShowShare = { showShareBottomSheet = true },
                    onRefresh = viewModel::loadLogs,
                    onClearLogs = viewModel::clearLogs,
                    onSearchChange = viewModel::onSearchTextChanged,
                    onToggleMethod = viewModel::toggleMethod,
                    onToggleStatusRange = viewModel::toggleStatusRange,
                    onRemoveMethod = viewModel::removeMethodFilter,
                    onRemoveStatusRange = viewModel::removeStatusRangeFilter,
                    onClearHost = viewModel::clearHostFilter,
                    onClearTimeRange = viewModel::clearTimeRangeFilter,
                    onClearResponseTime = viewModel::clearResponseTimeFilter,
                    onClearFailedOnly = viewModel::clearFailedOnlyFilter,
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val currentDestination = navigator.currentDestination
                val selectedLogId = currentDestination?.contentKey
                val selectedLog = uiState.logs.find { it.id == selectedLogId }

                if (selectedLog != null) {
                    NetworkDetailContent(
                        log = selectedLog,
                        onBack = {
                            scope.launch {
                                navigator.navigateBack()
                            }
                        },
                    )
                } else {
                    EmptyDetailPane("Select a network request to view details")
                }
            }
        },
    )

    // Filter bottom sheet
    if (showFilterSheet) {
        NetworkFilterSheet(
            filter = uiState.advancedFilter,
            onFilterChange = viewModel::updateAdvancedFilter,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkListContent(
    uiState: NetworkLogsUiState,
    onLogClick: (NetworkLogEntry) -> Unit,
    onShowFilter: () -> Unit,
    onShowShare: () -> Unit,
    onRefresh: () -> Unit,
    onClearLogs: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleMethod: (String) -> Unit,
    onToggleStatusRange: (String) -> Unit,
    onRemoveMethod: (String) -> Unit,
    onRemoveStatusRange: (String) -> Unit,
    onClearHost: () -> Unit,
    onClearTimeRange: () -> Unit,
    onClearResponseTime: () -> Unit,
    onClearFailedOnly: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network") },
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
                placeholder = "Search URL or host...",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (uiState.hasActiveFilters) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.selectedMethods.forEach { method ->
                        ActiveFilterBadge(label = method, onRemove = { onRemoveMethod(method) })
                    }
                    uiState.selectedStatusRanges.forEach { range ->
                        ActiveFilterBadge(label = range, onRemove = { onRemoveStatusRange(range) })
                    }
                    if (uiState.advancedFilter.hostPattern.isNotEmpty()) {
                        ActiveFilterBadge(label = "Host: ${uiState.advancedFilter.hostPattern}", onRemove = onClearHost)
                    }
                    if (uiState.advancedFilter.fromTimestamp != null || uiState.advancedFilter.toTimestamp != null) {
                        ActiveFilterBadge(label = "Time Range", onRemove = onClearTimeRange)
                    }
                    if (uiState.advancedFilter.responseTimeThreshold != null) {
                        ActiveFilterBadge(label = "> ${uiState.advancedFilter.responseTimeThreshold}ms", onRemove = onClearResponseTime)
                    }
                    if (uiState.advancedFilter.showOnlyFailed) {
                        ActiveFilterBadge(label = "Failed Only", onRemove = onClearFailedOnly)
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
                        icon = if (uiState.logs.isEmpty()) Icons.Default.CloudQueue else Icons.Default.Search,
                        message = if (uiState.logs.isEmpty()) "No network logs to display" else "No matching requests",
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.filteredLogs, key = { it.id }) { log ->
                            NetworkLogRow(log = log, onClick = { onLogClick(log) })
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
private fun NetworkDetailContent(
    log: NetworkLogEntry,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NetworkDetailPane(log = log)
        }
    }
}

@Composable
fun NetworkLogRow(
    log: NetworkLogEntry,
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
                StatusBadge(code = log.responseCode, error = log.error)
                Text(
                    text = log.method,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
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
            text = log.url,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${log.duration} ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (log.source != "unknown") {
                Text(
                    text = log.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
            }
        }
    }
}

private fun formatShortTime(timestamp: kotlinx.datetime.Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')
    return "$hour:$minute:$second"
}
