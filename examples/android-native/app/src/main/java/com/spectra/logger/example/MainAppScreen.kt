package com.spectra.logger.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spectra.logger.SpectraLogger
import com.spectra.logger.ui.screens.SpectraLoggerScreen

/**
 * Main app screen - demonstrates how to integrate Spectra Logger
 *
 * Users only need one button that opens the complete SpectraLoggerScreen from the SDK
 */
@Composable
fun MainAppScreen() {
    var showLogger by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // App branding
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = "Example App",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "with Spectra Logger Integration",
            fontSize = 14.sp,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.size(48.dp))

        // Example actions
        Text(
            text = "Example Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        OutlinedButton(
            onClick = {
                tapCount++
                SpectraLogger.i("UserAction", "Button tapped $tapCount times")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tap Me (Generates Log)")
        }

        OutlinedButton(
            onClick = {
                SpectraLogger.w("Warning", "Example warning log generated")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generate Warning")
        }

        OutlinedButton(
            onClick = {
                SpectraLogger.e("Error", "Example error log generated")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generate Error")
        }

        Spacer(modifier = Modifier.size(48.dp))

        // Open Spectra Logger button - this is the only SDK integration needed!
        Button(
            onClick = {
                showLogger = true
                SpectraLogger.i("Navigation", "Opening Spectra Logger UI")
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
        ) {
            Text(
                "Open Spectra Logger",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }

    // Present the complete Spectra Logger screen from the SDK
    // It comes with all tabs (Logs, Network, Settings) built-in
    // Users just show it - no need to build tabs themselves
    if (showLogger) {
        Dialog(
            onDismissRequest = { showLogger = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            SpectraLoggerScreen()
        }
    }
}
