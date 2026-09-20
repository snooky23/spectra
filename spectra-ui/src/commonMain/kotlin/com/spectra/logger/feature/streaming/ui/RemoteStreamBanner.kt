package com.spectra.logger.feature.streaming.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.feature.streaming.model.StreamConnectionState

/**
 * Sticky banner alerting the user when remote streaming is active or pending authorization.
 */
@Composable
fun RemoteStreamBanner(
    connectionState: StreamConnectionState,
    activeSessionId: String?,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = connectionState != StreamConnectionState.DISCONNECTED

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        val (bgColor, dotColor, statusText) =
            when (connectionState) {
                StreamConnectionState.STREAMING ->
                    Triple(
                        MaterialTheme.colorScheme.primaryContainer,
                        Color(0xFF4CAF50),
                        "Streaming to Desktop" + (activeSessionId?.let { " ($it)" } ?: ""),
                    )
                StreamConnectionState.CONNECTING ->
                    Triple(
                        MaterialTheme.colorScheme.surfaceVariant,
                        Color(0xFF2196F3),
                        "Connecting to Desktop...",
                    )
                StreamConnectionState.AWAITING_AUTHORIZATION ->
                    Triple(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        Color(0xFFFF9800),
                        "Awaiting Desktop Authorization...",
                    )
                StreamConnectionState.REJECTED ->
                    Triple(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.error,
                        "Desktop Rejected Connection",
                    )
                StreamConnectionState.ERROR ->
                    Triple(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.error,
                        "Streaming Error Occurred",
                    )
                StreamConnectionState.DISCONNECTED ->
                    Triple(
                        Color.Transparent,
                        Color.Transparent,
                        "",
                    )
            }

        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .background(dotColor, CircleShape),
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            TextButton(
                onClick = onDisconnect,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor =
                            if (connectionState == StreamConnectionState.STREAMING) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    ),
            ) {
                Text(
                    text =
                        when (connectionState) {
                            StreamConnectionState.STREAMING -> "Disconnect"
                            StreamConnectionState.AWAITING_AUTHORIZATION,
                            StreamConnectionState.CONNECTING,
                            -> "Cancel"
                            else -> "Dismiss"
                        },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
