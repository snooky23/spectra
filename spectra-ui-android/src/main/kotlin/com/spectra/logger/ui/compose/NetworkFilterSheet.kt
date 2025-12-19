package com.spectra.logger.ui.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Filter configuration for Network screen
 */
data class NetworkFilterConfig(
    val selectedMethods: Set<String> = emptySet(),
    val selectedStatusRanges: Set<String> = emptySet(),
    val hostPattern: String = "",
    val fromTimestamp: Instant? = null,
    val toTimestamp: Instant? = null,
    val responseTimeThreshold: ResponseTimeThreshold? = null,
    val showOnlyFailed: Boolean = false,
) {
    enum class ResponseTimeThreshold(val label: String, val milliseconds: Long) {
        OVER_100MS("> 100ms", 100),
        OVER_500MS("> 500ms", 500),
        OVER_1S("> 1s", 1000),
    }

    val hasActiveFilters: Boolean
        get() =
            selectedMethods.isNotEmpty() ||
                selectedStatusRanges.isNotEmpty() ||
                hostPattern.isNotEmpty() ||
                fromTimestamp != null ||
                toTimestamp != null ||
                responseTimeThreshold != null ||
                showOnlyFailed

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedMethods.isNotEmpty()) count++
            if (selectedStatusRanges.isNotEmpty()) count++
            if (hostPattern.isNotEmpty()) count++
            if (fromTimestamp != null || toTimestamp != null) count++
            if (responseTimeThreshold != null) count++
            if (showOnlyFailed) count++
            return count
        }
}

/**
 * Filter bottom sheet for advanced network log filtering
 * Matches iOS NetworkFilterView design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkFilterSheet(
    filter: NetworkFilterConfig,
    onFilterChange: (NetworkFilterConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    var localFilter by remember { mutableStateOf(filter) }

    val httpMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
    val statusRanges = listOf("2xx", "3xx", "4xx", "5xx")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
        ) {
            // Header
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(
                    text = "Network Filters",
                    style = MaterialTheme.typography.titleLarge,
                )
                TextButton(
                    onClick = {
                        localFilter = NetworkFilterConfig()
                    },
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider()

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
            ) {
                // HTTP Methods Section
                Text(
                    text = "HTTP METHODS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )

                Row(
                    modifier =
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    httpMethods.forEach { method ->
                        FilterChip(
                            selected = localFilter.selectedMethods.contains(method),
                            onClick = {
                                localFilter =
                                    if (localFilter.selectedMethods.contains(method)) {
                                        localFilter.copy(selectedMethods = localFilter.selectedMethods - method)
                                    } else {
                                        localFilter.copy(selectedMethods = localFilter.selectedMethods + method)
                                    }
                            },
                            label = { Text(method) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Status Codes Section
                Text(
                    text = "STATUS CODES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                Row(
                    modifier =
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    statusRanges.forEach { range ->
                        FilterChip(
                            selected = localFilter.selectedStatusRanges.contains(range),
                            onClick = {
                                localFilter =
                                    if (localFilter.selectedStatusRanges.contains(range)) {
                                        localFilter.copy(
                                            selectedStatusRanges = localFilter.selectedStatusRanges - range,
                                        )
                                    } else {
                                        localFilter.copy(
                                            selectedStatusRanges = localFilter.selectedStatusRanges + range,
                                        )
                                    }
                            },
                            label = { Text(range) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorForStatusRange(range).copy(alpha = 0.2f),
                                    selectedLabelColor = colorForStatusRange(range),
                                ),
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Host / Domain Section
                Text(
                    text = "HOST / DOMAIN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                OutlinedTextField(
                    value = localFilter.hostPattern,
                    onValueChange = { localFilter = localFilter.copy(hostPattern = it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    placeholder = { Text("Filter by host pattern...") },
                    supportingText = { Text("Supports wildcards: api.*, *.example.com") },
                    singleLine = true,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Time Range Section
                Text(
                    text = "TIME RANGE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                // Quick presets
                Row(
                    modifier =
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = {
                            localFilter =
                                localFilter.copy(
                                    fromTimestamp = Clock.System.now() - 1.hours,
                                    toTimestamp = null,
                                )
                        },
                        label = { Text("Last hour") },
                    )
                    AssistChip(
                        onClick = {
                            localFilter =
                                localFilter.copy(
                                    fromTimestamp = Clock.System.now() - 1.days,
                                    toTimestamp = null,
                                )
                        },
                        label = { Text("Today") },
                    )
                    AssistChip(
                        onClick = {
                            localFilter =
                                localFilter.copy(
                                    fromTimestamp = Clock.System.now() - 1.days,
                                    toTimestamp = null,
                                )
                        },
                        label = { Text("Last 24h") },
                    )
                    AssistChip(
                        onClick = {
                            localFilter =
                                localFilter.copy(
                                    fromTimestamp = Clock.System.now() - 7.days,
                                    toTimestamp = null,
                                )
                        },
                        label = { Text("Last 7 days") },
                    )
                    AssistChip(
                        onClick = {
                            localFilter =
                                localFilter.copy(
                                    fromTimestamp = null,
                                    toTimestamp = null,
                                )
                        },
                        label = { Text("Clear") },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Response Time Section
                Text(
                    text = "RESPONSE TIME",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                NetworkFilterConfig.ResponseTimeThreshold.entries.forEach { threshold ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = localFilter.responseTimeThreshold == threshold,
                            onClick = {
                                localFilter =
                                    if (localFilter.responseTimeThreshold == threshold) {
                                        localFilter.copy(responseTimeThreshold = null)
                                    } else {
                                        localFilter.copy(responseTimeThreshold = threshold)
                                    }
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(threshold.label)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Failed Requests Toggle
                Text(
                    text = "ERRORS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Show only failed requests")
                    Switch(
                        checked = localFilter.showOnlyFailed,
                        onCheckedChange = { localFilter = localFilter.copy(showOnlyFailed = it) },
                    )
                }

                Text(
                    text = "Filter to show only requests with 4xx/5xx status codes or errors",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // Apply button
            Button(
                onClick = {
                    onFilterChange(localFilter)
                    onDismiss()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text("Apply Filters")
            }
        }
    }
}
