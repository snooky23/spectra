package com.spectra.logger.core.ui.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spectra.logger.core.ui.compose.SpectraLoggerScreen

/**
 * Desktop application entry point for Compose Hot Reload and AI agent MCP interaction.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Spectra Logger - Hot Reload",
        state = rememberWindowState(width = 1100.dp, height = 800.dp),
    ) {
        SpectraLoggerScreen(
            onDismiss = ::exitApplication,
        )
    }
}
