package com.spectra.logger.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Filter bottom sheet for advanced log filtering
 * Matches iOS LogsFilterView design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsFilterSheet(
    filter: AdvancedFilter,
    availableTags: List<String>,
    onFilterChange: (AdvancedFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var localFilter by remember { mutableStateOf(filter) }
    var customTagInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(
                    onClick = { localFilter = AdvancedFilter() }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Tags Section
                Text(
                    text = "TAGS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )

                // Custom tag input
                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Add custom tag...") },
                    trailingIcon = {
                        if (customTagInput.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val tag = customTagInput.trim()
                                    if (tag.isNotEmpty() && !localFilter.customTags.contains(tag)) {
                                        localFilter = localFilter.copy(
                                            customTags = localFilter.customTags + tag
                                        )
                                    }
                                    customTagInput = ""
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom tags
                localFilter.customTags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = {
                                localFilter = localFilter.copy(
                                    customTags = localFilter.customTags - tag
                                )
                            }
                        )
                        Text(tag)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "(custom)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Existing tags
                availableTags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = localFilter.selectedTags.contains(tag),
                            onCheckedChange = { checked ->
                                localFilter = if (checked) {
                                    localFilter.copy(selectedTags = localFilter.selectedTags + tag)
                                } else {
                                    localFilter.copy(selectedTags = localFilter.selectedTags - tag)
                                }
                            }
                        )
                        Text(tag)
                    }
                }

                if (availableTags.isEmpty() && localFilter.customTags.isEmpty()) {
                    Text(
                        text = "No tags available. Add a custom tag to filter for future logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Time Range Section
                Text(
                    text = "TIME RANGE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Quick presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {
                            localFilter = localFilter.copy(
                                fromTimestamp = Clock.System.now() - 1.hours,
                                toTimestamp = null
                            )
                        },
                        label = { Text("Last hour") }
                    )
                    AssistChip(
                        onClick = {
                            localFilter = localFilter.copy(
                                fromTimestamp = Clock.System.now() - 1.days,
                                toTimestamp = null
                            )
                        },
                        label = { Text("Last 24h") }
                    )
                    AssistChip(
                        onClick = {
                            localFilter = localFilter.copy(
                                fromTimestamp = Clock.System.now() - 7.days,
                                toTimestamp = null
                            )
                        },
                        label = { Text("Last 7 days") }
                    )
                    AssistChip(
                        onClick = {
                            localFilter = localFilter.copy(
                                fromTimestamp = null,
                                toTimestamp = null
                            )
                        },
                        label = { Text("Clear") }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Metadata Section
                Text(
                    text = "METADATA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                OutlinedTextField(
                    value = localFilter.metadataKey,
                    onValueChange = { localFilter = localFilter.copy(metadataKey = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Key (e.g., user_id)") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = localFilter.metadataValue,
                    onValueChange = { localFilter = localFilter.copy(metadataValue = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Value (e.g., 12345)") },
                    singleLine = true
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Error Toggle
                Text(
                    text = "ERRORS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show only logs with errors")
                    Switch(
                        checked = localFilter.hasErrorOnly,
                        onCheckedChange = { localFilter = localFilter.copy(hasErrorOnly = it) }
                    )
                }
            }

            // Apply button
            Button(
                onClick = {
                    onFilterChange(localFilter)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Apply Filters")
            }
        }
    }
}
