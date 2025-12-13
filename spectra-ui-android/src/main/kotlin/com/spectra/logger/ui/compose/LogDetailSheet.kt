package com.spectra.logger.ui.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.ui.compose.components.DetailSection
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Log detail sheet showing full log information
 * Matches iOS LogDetailView design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailSheet(
    log: LogEntry,
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    var stackTraceExpanded by remember { mutableStateOf(true) }

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LogLevelBadge(level = log.level)
                    Text(
                        text = formatFullTime(log.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(48.dp)) // Balance the close button
            }

            HorizontalDivider()

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Tag section
                DetailSection(title = "Tag") {
                    Text(text = log.tag)
                }

                // Message section
                DetailSection(title = "Message") {
                    Text(text = log.message)
                }

                // Stack trace section (if present)
                // throwable is stored as a pre-formatted String
                val stackTrace = log.throwable ?: log.metadata["stack_trace"]

                if (stackTrace != null) {
                    ExpandableStackTraceSection(
                        stackTrace = stackTrace,
                        expanded = stackTraceExpanded,
                        onToggle = { stackTraceExpanded = !stackTraceExpanded },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(stackTrace))
                        },
                    )
                }

                // Metadata section
                val displayMetadata = log.metadata.filterKeys { it != "stack_trace" }
                if (displayMetadata.isNotEmpty()) {
                    DetailSection(title = "Metadata") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            displayMetadata.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expandable stack trace section with line numbers
 */
@Composable
private fun ExpandableStackTraceSection(
    stackTrace: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit,
) {
    val lines = stackTrace.lines()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onToggle) {
                Text(
                    text = "STACK TRACE (${lines.size} lines)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Content
        if (expanded) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            ) {
                Row(
                    modifier =
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                ) {
                    // Line numbers
                    Column(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        lines.forEachIndexed { index, _ ->
                            Text(
                                text = String.format("%3d", index + 1),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Stack trace content
                    Column {
                        lines.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatFullTime(timestamp: kotlinx.datetime.Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    return String.format(
        "%04d-%02d-%02d %02d:%02d:%02d",
        localDateTime.year,
        localDateTime.monthNumber,
        localDateTime.dayOfMonth,
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.second,
    )
}

private fun formatStackTrace(throwable: Throwable): String {
    return buildString {
        appendLine("${throwable::class.simpleName}: ${throwable.message}")
        throwable.stackTrace.forEach { element ->
            appendLine("    at $element")
        }
    }
}
