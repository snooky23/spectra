package com.spectra.logger.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.ui.theme.LogColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Dialog showing detailed information about a log entry.
 *
 * @param entry The log entry to display
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
@Suppress("FunctionName")
fun LogDetailDialog(
    entry: LogEntry,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row {
                Text(
                    text = entry.level.name,
                    color = LogColors.getColor(entry.level),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• ${entry.tag}",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                // Timestamp
                DetailRow(
                    label = "Time",
                    value = formatDetailedTime(entry.timestamp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ID
                DetailRow(
                    label = "ID",
                    value = entry.id,
                    valueStyle = FontFamily.Monospace,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = "Message",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp),
                )

                // Throwable
                if (entry.throwable != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Exception",
                        style = MaterialTheme.typography.labelMedium,
                        color = LogColors.Error,
                    )
                    Text(
                        text = entry.throwable,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = LogColors.Error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // Metadata
                if (entry.metadata.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Metadata",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    entry.metadata.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Text(
                                text = "$key: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueStyle: FontFamily? = null,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = valueStyle,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private fun formatDetailedTime(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val year = localDateTime.year
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')
    val millis = (localDateTime.nanosecond / 1_000_000).toString().padStart(3, '0')
    return "$year-$month-$day $hour:$minute:$second.$millis"
}
