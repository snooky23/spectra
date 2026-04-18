package com.spectra.logger.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.ui.compose.components.*

/**
 * Network logs screen with list and detail views.
 * Simplified for multiplatform compilation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogsScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkLogsViewModel = viewModel { NetworkLogsViewModel() },
    onDismiss: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        com.spectra.logger.ui.compose.navigation.AdaptiveNavigator<NetworkLogEntry>(
            listContent = { navigateToDetail, isDualPane ->
                NetworkLogsListContent(
                    uiState = uiState,
                    onLogClick = navigateToDetail,
                    onDismiss = onDismiss,
                    isDualPane = isDualPane,
                    onShowFilter = { showFilterSheet = true },
                    onRefresh = viewModel::loadLogs,
                    onClearLogs = viewModel::clearLogs,
                    onSearchChange = viewModel::onSearchTextChanged,
                )
            },
            detailContent = { selectedItem, navigateBack, isDualPane ->
                NetworkLogDetailContent(
                    log = selectedItem,
                    onBack = navigateBack,
                    isDualPane = isDualPane,
                )
            }
        )

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
    onDismiss: () -> Unit,
    isDualPane: Boolean,
    onShowFilter: () -> Unit,
    onRefresh: () -> Unit,
    onClearLogs: () -> Unit,
    onSearchChange: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            SpectraNavBar(
                title = "Network Logs",
                navMode = NavMode.ROOT,
                isDualPane = isDualPane,
                onDismiss = onDismiss,
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
        val horizontalPadding = if (isDualPane)
            com.spectra.logger.ui.theme.SpectraDesignTokens.ScreenHorizontalPaddingExpanded
        else
            com.spectra.logger.ui.theme.SpectraDesignTokens.ScreenHorizontalPaddingCompact
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            com.spectra.logger.ui.compose.components.SearchBar(
                query = uiState.searchText,
                onQueryChange = onSearchChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = 8.dp),
                placeholder = "Search URL...",
            )

            if (uiState.filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding),
                ) {
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
    isDualPane: Boolean = false,
) {
    Scaffold(
        topBar = {
            SpectraNavBar(
                title = "Request Detail",
                navMode = NavMode.DETAIL,
                isDualPane = isDualPane,
                onBack = onBack,
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
