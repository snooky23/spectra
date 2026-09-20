package com.spectra.logger.feature.events.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.core.ui.theme.SpectraDesignTokens
import com.spectra.logger.core.ui.util.PlatformUtils
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Detailed view of an event or screen view session.
 */
@Composable
fun EventDetailPane(
    event: EventLogEntry,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EventTypeBadge(type = event.eventType)
            Text(
                text = formatEventTime(event.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Event Name
        DetailRow(label = "Name", value = event.name)

        // Duration (if available)
        if (event.durationMs != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Duration", value = "${event.durationMs} ms")
        }

        Spacer(modifier = Modifier.height(8.dp))
        DetailRow(label = "Source", value = event.source)
        DetailRow(label = "Source Type", value = event.sourceType.name)

        // Parameters Section
        if (event.parameters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Parameters",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            event.parameters.forEach { (key, value) ->
                ParamRow(key = key, value = value)
            }
        }

        // Action Buttons
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    val formatted =
                        buildString {
                            appendLine("Event: ${event.name}")
                            appendLine("Type: ${event.eventType}")
                            appendLine("Time: ${formatEventTime(event.timestamp)}")
                            if (event.durationMs != null) appendLine("Duration: ${event.durationMs} ms")
                            appendLine("Source: ${event.source} (${event.sourceType})")
                            if (event.parameters.isNotEmpty()) {
                                appendLine("Parameters:")
                                event.parameters.forEach { (k, v) -> appendLine("  $k: $v") }
                            }
                        }
                    PlatformUtils.shareText(text = formatted, title = "Share Event")
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Event")
            }
        }
    }
}

@Composable
fun EventTypeBadge(
    type: EventType,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor, label) =
        when (type) {
            EventType.SCREEN_VIEW ->
                Triple(
                    SpectraDesignTokens.InfoGreen.copy(alpha = 0.15f),
                    SpectraDesignTokens.InfoGreen,
                    "SCREEN",
                )
            EventType.USER_ACTION ->
                Triple(
                    SpectraDesignTokens.WarningOrange.copy(alpha = 0.15f),
                    SpectraDesignTokens.WarningOrange,
                    "ACTION",
                )
            EventType.LIFECYCLE ->
                Triple(
                    SpectraDesignTokens.FatalPurple.copy(alpha = 0.15f),
                    SpectraDesignTokens.FatalPurple,
                    "LIFECYCLE",
                )
            EventType.CUSTOM ->
                Triple(
                    SpectraDesignTokens.DebugBlue.copy(alpha = 0.15f),
                    SpectraDesignTokens.DebugBlue,
                    "CUSTOM",
                )
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ParamRow(
    key: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Text(
            text = "$key:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private fun formatEventTime(timestamp: Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')
    return "${localDateTime.date} $hour:$minute:$second"
}
