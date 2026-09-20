package com.spectra.logger.feature.events.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.core.ui.components.common.NavMode
import com.spectra.logger.core.ui.components.common.SearchBar
import com.spectra.logger.core.ui.components.common.SpectraNavBar
import com.spectra.logger.core.ui.navigation.AdaptiveNavigator
import com.spectra.logger.core.ui.theme.SpectraDesignTokens
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Events Screen hosting the timeline view and detail pane.
 * Adheres to Spectra's Adaptive UI Architecture (Dual-pane on tablets/desktop, stack on phones).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = viewModel { EventsViewModel() },
    onDismiss: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AdaptiveNavigator<EventLogEntry>(
            listContent = { navigateToDetail, isDualPane ->
                EventsListContent(
                    uiState = uiState,
                    onEventClick = navigateToDetail,
                    onDismiss = onDismiss,
                    isDualPane = isDualPane,
                    onShowFilter = { showFilterSheet = true },
                    onRefresh = viewModel::loadEvents,
                    onClearEvents = viewModel::clearEvents,
                    onSearchChange = viewModel::onSearchTextChanged,
                    onToggleEventType = viewModel::toggleEventType,
                    onExportEvents = viewModel::exportEvents,
                )
            },
            detailContent = { selectedItem, navigateBack, isDualPane ->
                EventDetailContent(
                    event = selectedItem,
                    onBack = navigateBack,
                    isDualPane = isDualPane,
                )
            },
        )

        if (showFilterSheet) {
            EventsFilterSheet(
                filter = uiState.advancedFilter,
                onFilterChange = viewModel::updateFilter,
                onDismiss = { showFilterSheet = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventsListContent(
    uiState: EventsUiState,
    onEventClick: (EventLogEntry) -> Unit,
    onDismiss: () -> Unit,
    isDualPane: Boolean,
    onShowFilter: () -> Unit,
    onRefresh: () -> Unit,
    onClearEvents: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleEventType: (EventType) -> Unit,
    onExportEvents: (com.spectra.logger.feature.logs.export.ExportFormat) -> Unit,
) {
    Scaffold(
        topBar = {
            SpectraNavBar(
                title = "Events",
                subtitle = "${uiState.filteredEvents.size} events",
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
                            text = { Text("Export as Markdown") },
                            onClick = {
                                onExportEvents(com.spectra.logger.feature.logs.export.ExportFormat.MARKDOWN)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Export as CSV") },
                            onClick = {
                                onExportEvents(com.spectra.logger.feature.logs.export.ExportFormat.CSV)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Export as JSON") },
                            onClick = {
                                onExportEvents(com.spectra.logger.feature.logs.export.ExportFormat.JSON)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Events") },
                            onClick = {
                                onClearEvents()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        val horizontalPadding =
            if (isDualPane) {
                SpectraDesignTokens.ScreenHorizontalPaddingExpanded
            } else {
                SpectraDesignTokens.ScreenHorizontalPaddingCompact
            }

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Search Bar
            SearchBar(
                query = uiState.searchText,
                onQueryChange = onSearchChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 8.dp),
                placeholder = "Search events or parameters...",
            )

            // Quick Filter Chips for Event Types
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EventType.entries.forEach { type ->
                    val isSelected = uiState.selectedEventTypes.contains(type)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleEventType(type) },
                        label = { Text(type.name.replace("_", " ")) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))

            // Events Timeline List
            if (uiState.filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.events.isEmpty()) "No events recorded yet" else "No matching events",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 4.dp),
                ) {
                    items(uiState.filteredEvents, key = { it.id }) { event ->
                        EventRow(event = event, onClick = { onEventClick(event) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun EventRow(
    event: EventLogEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Event Type Badge
        EventTypeBadge(type = event.eventType)

        Spacer(modifier = Modifier.width(12.dp))

        // Center: Name & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = formatEventTime(event.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                if (event.parameters.isNotEmpty()) {
                    Text(
                        text = "• ${event.parameters.size} params",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    )
                }
            }
        }

        // Right side: Duration Pill (if tracked)
        if (event.durationMs != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SpectraDesignTokens.SystemGray6,
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(
                    text = "${event.durationMs}ms",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: EventLogEntry,
    onBack: () -> Unit,
    isDualPane: Boolean = false,
) {
    Scaffold(
        topBar = {
            SpectraNavBar(
                title = "Event Details",
                navMode = NavMode.DETAIL,
                isDualPane = isDualPane,
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            EventDetailPane(event = event)
        }
    }
}

private fun formatEventTime(timestamp: Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')
    return "$hour:$minute:$second"
}
