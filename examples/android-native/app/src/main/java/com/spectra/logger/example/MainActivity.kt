package com.spectra.logger.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Example activity demonstrating Spectra Logger usage.
 * Shows a simple interface with tabs for Actions and Network requests.
 * Includes an "Open Spectra Logger" button to view the captured logs.
 */
class MainActivity : ComponentActivity() {
    private val showSpectraLogger = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureLogger()
        generateSampleLogs()

        setContent {
            MaterialTheme {
                if (showSpectraLogger.value) {
                    SpectraLoggerScreen(onClose = { showSpectraLogger.value = false })
                } else {
                    MainAppScreen(onOpenSpectra = { showSpectraLogger.value = true })
                }
            }
        }
    }

    private fun configureLogger() {
        SpectraLogger.configure {
            minLogLevel = LogLevel.VERBOSE
            logStorage {
                maxCapacity = MAX_LOG_STORAGE_CAPACITY
            }
        }
    }

    private fun generateSampleLogs() {
        lifecycleScope.launch {
            SpectraLogger.i("App", "Spectra Logger Example Started")
            SpectraLogger.i("App", "Version: ${SpectraLogger.getVersion()}")

            delay(DELAY_SHORT)

            SpectraLogger.v("UI", "MainActivity created")
            SpectraLogger.v("Lifecycle", "onCreate called")

            SpectraLogger.d("Init", "Initializing example app")
            SpectraLogger.d(
                "Config",
                "Logger configured with min level: ${SpectraLogger.configuration.minLogLevel}",
            )

            delay(DELAY_MEDIUM)

            SpectraLogger.i(
                "User",
                "User opened the app",
                metadata = mapOf("user_id" to "12345"),
            )
            SpectraLogger.i("Navigation", "Showing actions screen")

            SpectraLogger.w("Performance", "Large dataset detected (1000+ items)")
            SpectraLogger.w("Memory", "Memory usage: 45MB / 128MB")
        }
    }

    private companion object {
        private const val MAX_LOG_STORAGE_CAPACITY = 20_000
        private const val DELAY_SHORT = 500L
        private const val DELAY_MEDIUM = 1000L
    }
}
