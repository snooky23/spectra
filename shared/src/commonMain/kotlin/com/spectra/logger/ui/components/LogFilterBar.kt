package com.spectra.logger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.ui.theme.LogColors

/**
 * Filter bar for logs with search and level filtering.
 *
 * @param searchText Current search text
 * @param onSearchTextChange Callback when search text changes
 * @param selectedLevels Currently selected log levels
 * @param onLevelToggle Callback when a log level is toggled
 * @param modifier Modifier for the component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogFilterBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedLevels: Set<LogLevel>,
    onLevelToggle: (LogLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        // Search field
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            label = { Text("Search logs") },
            placeholder = { Text("Search by message or tag") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        // Log level filter chips
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LogLevel.entries.forEach { level ->
                FilterChip(
                    selected = level in selectedLevels,
                    onClick = { onLevelToggle(level) },
                    label = {
                        Text(
                            text = level.name,
                            color = LogColors.getColor(level),
                        )
                    },
                )
            }
        }
    }
}
