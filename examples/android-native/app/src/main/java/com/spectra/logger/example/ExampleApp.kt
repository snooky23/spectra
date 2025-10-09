package com.spectra.logger.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spectra.logger.SpectraLogger

/**
 * Simple example app demonstrating SpectraLogger usage.
 *
 * This is a minimal example showing how to:
 * 1. Log messages at different levels
 * 2. Log with tags
 * 3. Log exceptions
 * 4. Generate network traffic (for network logging demo)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleApp() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SpectraLogger Example") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "SpectraLogger Demo",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            Button(
                onClick = { logDebugMessage() },
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text("Log Debug Message")
            }

            Button(
                onClick = { logInfoMessage() },
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text("Log Info Message")
            }

            Button(
                onClick = { logWarningMessage() },
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text("Log Warning Message")
            }

            Button(
                onClick = { logErrorMessage() },
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text("Log Error Message")
            }

            Button(
                onClick = { logWithException() },
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text("Log with Exception")
            }

            Text(
                text = "Check logcat to see the logged messages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

private fun logDebugMessage() {
    SpectraLogger.d("ExampleApp", "This is a debug message")
}

private fun logInfoMessage() {
    SpectraLogger.i("ExampleApp", "User performed an action")
}

private fun logWarningMessage() {
    SpectraLogger.w("ExampleApp", "This might be a problem")
}

private fun logErrorMessage() {
    SpectraLogger.e("ExampleApp", "An error occurred!")
}

private fun logWithException() {
    try {
        throw IllegalStateException("Example exception for logging")
    } catch (e: Exception) {
        SpectraLogger.e("ExampleApp", "Caught an exception", e)
    }
}
