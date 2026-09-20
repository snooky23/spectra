package com.spectra.logger.feature.events.ui

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
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventType

/**
 * Filter sheet for advanced event and screen filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsFilterSheet(
    filter: EventFilter,
    onFilterChange: (EventFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTypes by remember { mutableStateOf(filter.eventTypes ?: emptySet()) }
    var minDurationMs by remember { mutableStateOf(filter.minDurationMs) }

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
                    text = "Filter Events",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(
                    onClick = {
                        selectedTypes = emptySet()
                        minDurationMs = null
                        onFilterChange(EventFilter.NONE)
                    },
                ) {
                    Text("Reset")
                }
            }

            HorizontalDivider()

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
            ) {
                // Event Type Section
                Text(
                    text = "Event Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EventType.entries.forEach { type ->
                        val isSelected = selectedTypes.contains(type)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTypes =
                                    if (isSelected) {
                                        selectedTypes - type
                                    } else {
                                        selectedTypes + type
                                    }
                            },
                            label = { Text(type.name.replace("_", " ")) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Duration Threshold Section
                Text(
                    text = "Min Duration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        null to "All",
                        100L to "> 100ms",
                        500L to "> 500ms",
                        1000L to "> 1s",
                        3000L to "> 3s",
                    ).forEach { (threshold, label) ->
                        val isSelected = minDurationMs == threshold
                        FilterChip(
                            selected = isSelected,
                            onClick = { minDurationMs = threshold },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Apply Button
                Button(
                    onClick = {
                        onFilterChange(
                            filter.copy(
                                eventTypes = if (selectedTypes.isEmpty()) null else selectedTypes,
                                minDurationMs = minDurationMs,
                            ),
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
