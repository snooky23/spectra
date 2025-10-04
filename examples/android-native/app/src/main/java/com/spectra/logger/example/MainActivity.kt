package com.spectra.logger.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Example activity demonstrating Spectra Logger usage.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureLogger()

        // Generate sample logs
        generateSampleLogs()

        setContent {
            MaterialTheme {
                ExampleApp()
            }
        }
    }

    private fun configureLogger() {
        SpectraLogger.configure {
            minLogLevel = LogLevel.VERBOSE
            logStorage {
                maxCapacity = MAX_LOG_STORAGE_CAPACITY
            }
            networkStorage {
                maxCapacity = MAX_NETWORK_STORAGE_CAPACITY
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
            SpectraLogger.d("Config", "Logger configured with min level: ${SpectraLogger.configuration.minLogLevel}")

            delay(DELAY_MEDIUM)

            SpectraLogger.i("User", "User opened the app", metadata = mapOf("user_id" to "12345"))
            SpectraLogger.i("Navigation", "Showing logs screen")

            SpectraLogger.w("Performance", "Large dataset detected (1000+ items)")
            SpectraLogger.w("Memory", "Memory usage: 45MB / 128MB")

            delay(DELAY_LONG)

            try {
                error("This is a sample error for demonstration")
            } catch (e: IllegalStateException) {
                SpectraLogger.e("Error", "Caught exception during demo", e)
            }

            SpectraLogger.f(
                "Critical",
                "This is a critical error example",
                metadata = mapOf("severity" to "high", "impact" to "user_experience"),
            )

            delay(DELAY_EXTRA_LONG)

            repeat(SAMPLE_LOG_COUNT) { index ->
                when (index % MODULO_FOR_LOG_TYPES) {
                    LOG_TYPE_VERBOSE -> SpectraLogger.v("Sample", "Verbose log #$index")
                    LOG_TYPE_DEBUG -> SpectraLogger.d("Sample", "Debug log #$index")
                    LOG_TYPE_INFO -> SpectraLogger.i("Sample", "Info log #$index")
                    LOG_TYPE_WARNING -> SpectraLogger.w("Sample", "Warning log #$index")
                    LOG_TYPE_ERROR -> SpectraLogger.e("Sample", "Error log #$index")
                }
                delay(DELAY_BETWEEN_LOGS)
            }
        }
    }

    private companion object {
        private const val MAX_LOG_STORAGE_CAPACITY = 20_000
        private const val MAX_NETWORK_STORAGE_CAPACITY = 2_000
        private const val SAMPLE_LOG_COUNT = 20
        private const val MODULO_FOR_LOG_TYPES = 5
        private const val DELAY_SHORT = 500L
        private const val DELAY_MEDIUM = 1000L
        private const val DELAY_LONG = 1500L
        private const val DELAY_EXTRA_LONG = 2000L
        private const val DELAY_BETWEEN_LOGS = 100L
        private const val LOG_TYPE_VERBOSE = 0
        private const val LOG_TYPE_DEBUG = 1
        private const val LOG_TYPE_INFO = 2
        private const val LOG_TYPE_WARNING = 3
        private const val LOG_TYPE_ERROR = 4
    }
}
