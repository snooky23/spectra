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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spectra.logger.SpectraLogger

/**
 * Main app screen - demonstrates SpectraLogger integration
 *
 * This example shows:
 * 1. Logging user actions
 * 2. Logging different severity levels
 * 3. Simple integration (just call SpectraLogger methods)
 */
@Composable
fun MainAppScreen() {
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

        OutlinedButton(
            onClick = {
                try {
                    throw IllegalStateException("Test exception")
                } catch (e: Exception) {
                    SpectraLogger.e("Exception", "Test exception caught", e)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generate Exception")
        }

        Spacer(modifier = Modifier.size(24.dp))

        Button(
            onClick = {
                SpectraLogger.d("Debug", "Debug message - check logcat!")
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
        ) {
            Text(
                "Generate Debug Log",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = "Check logcat to see all logged messages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
