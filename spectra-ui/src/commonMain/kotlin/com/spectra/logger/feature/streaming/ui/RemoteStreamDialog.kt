package com.spectra.logger.feature.streaming.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.feature.streaming.model.StreamConnectionState

/**
 * Dialog for establishing a live WebSocket connection to a desktop companion dashboard.
 */
@Composable
fun RemoteStreamDialog(
    viewModel: RemoteStreamViewModel,
    onDismiss: () -> Unit,
    onLaunchScanner: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = "Remote Stream",
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = "Desktop Remote Streaming",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Stream logs and network telemetry in real-time to your desktop browser over local Wi-Fi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedButton(
                    onClick = onLaunchScanner,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR Code",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Scan Browser QR Code")
                }

                OutlinedTextField(
                    value = state.serverUrl,
                    onValueChange = { viewModel.onServerUrlChanged(it) },
                    label = { Text("Server WebSocket URL") },
                    placeholder = { Text("ws://192.168.1.100:9292/spectra-ws") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.token,
                    onValueChange = { viewModel.onTokenChanged(it) },
                    label = { Text("Pairing Token / Secret") },
                    placeholder = { Text("Optional or from QR code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                state.errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (state.connectionState == StreamConnectionState.AWAITING_AUTHORIZATION) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text(
                            text = "Please approve connection on desktop...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state.connectionState == StreamConnectionState.STREAMING) {
                Button(
                    onClick = { viewModel.disconnect() },
                ) {
                    Text("Disconnect")
                }
            } else {
                Button(
                    onClick = { viewModel.connect() },
                    enabled =
                        state.connectionState != StreamConnectionState.CONNECTING &&
                            state.connectionState != StreamConnectionState.AWAITING_AUTHORIZATION,
                ) {
                    Text(
                        if (state.connectionState == StreamConnectionState.CONNECTING ||
                            state.connectionState == StreamConnectionState.AWAITING_AUTHORIZATION
                        ) {
                            "Connecting..."
                        } else {
                            "Connect"
                        },
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}
