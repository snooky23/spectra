package com.spectra.logger.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.domain.model.NetworkLogEntry

/**
 * Network logs screen with list and detail views.
 * Simplified for multiplatform compilation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogsScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkLogsViewModel = viewModel { NetworkLogsViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedLog == null) {
            NetworkLogsListContent(
                uiState = uiState,
                onLogClick = { log ->
                    selectedLog = log
                },
                onShowFilter = { showFilterSheet = true },
                onRefresh = viewModel::loadLogs,
                onClearLogs = viewModel::clearLogs,
                onSearchChange = viewModel::onSearchTextChanged,
            )
        } else {
            NetworkLogDetailContent(
                log = selectedLog!!,
                onBack = {
                    selectedLog = null
                },
            )
        }

        if (showFilterSheet) {
            NetworkFilterSheet(
                filter = uiState.advancedFilter,
                onFilterChange = viewModel::updateAdvancedFilter,
                onDismiss = { showFilterSheet = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkLogsListContent(
    uiState: NetworkLogsUiState,
    onLogClick: (NetworkLogEntry) -> Unit,
    onShowFilter: () -> Unit,
    onRefresh: () -> Unit,
    onClearLogs: () -> Unit,
    onSearchChange: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Logs") },
                actions = {
                    IconButton(onClick = onShowFilter) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
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
                            text = { Text("Clear All") },
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = uiState.searchText,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search URL...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
            )

            if (uiState.filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs found")
                }
            } else {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkLogDetailContent(
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
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = log.method,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            com.spectra.logger.ui.compose.components.StatusBadge(code = log.responseCode, error = log.error)
        }
        Text(
            text = log.url,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
