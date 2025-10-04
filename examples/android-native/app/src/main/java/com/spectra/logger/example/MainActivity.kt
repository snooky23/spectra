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

        // Configure logger
        SpectraLogger.configure {
            minLogLevel = LogLevel.VERBOSE
            logStorage {
                maxCapacity = 20_000
            }
            networkStorage {
                maxCapacity = 2_000
            }
        }

        // Generate sample logs
        generateSampleLogs()

        setContent {
            MaterialTheme {
                ExampleApp()
            }
        }
    }

    private fun generateSampleLogs() {
        lifecycleScope.launch {
            SpectraLogger.i("App", "Spectra Logger Example Started")
            SpectraLogger.i("App", "Version: ${SpectraLogger.getVersion()}")

            delay(500)

            // Sample verbose logs
            SpectraLogger.v("UI", "MainActivity created")
            SpectraLogger.v("Lifecycle", "onCreate called")

            // Sample debug logs
            SpectraLogger.d("Init", "Initializing example app")
            SpectraLogger.d("Config", "Logger configured with min level: ${SpectraLogger.configuration.minLogLevel}")

            delay(1000)

            // Sample info logs
            SpectraLogger.i("User", "User opened the app", metadata = mapOf("user_id" to "12345"))
            SpectraLogger.i("Navigation", "Showing logs screen")

            // Sample warning logs
            SpectraLogger.w("Performance", "Large dataset detected (1000+ items)")
            SpectraLogger.w("Memory", "Memory usage: 45MB / 128MB")

            delay(1500)

            // Sample error log with throwable
            try {
                throw IllegalStateException("This is a sample error for demonstration")
            } catch (e: Exception) {
                SpectraLogger.e("Error", "Caught exception during demo", e)
            }

            // Sample fatal log
            SpectraLogger.f(
                "Critical",
                "This is a critical error example",
                metadata = mapOf("severity" to "high", "impact" to "user_experience"),
            )

            delay(2000)

            // Generate some additional logs
            repeat(SAMPLE_LOG_COUNT) { index ->
                when (index % MODULO_FOR_LOG_TYPES) {
                    0 -> SpectraLogger.v("Sample", "Verbose log #$index")
                    1 -> SpectraLogger.d("Sample", "Debug log #$index")
                    2 -> SpectraLogger.i("Sample", "Info log #$index")
                    3 -> SpectraLogger.w("Sample", "Warning log #$index")
                    4 -> SpectraLogger.e("Sample", "Error log #$index")
                }
                delay(100)
            }
        }
    }

    private companion object {
        private const val SAMPLE_LOG_COUNT = 20
        private const val MODULO_FOR_LOG_TYPES = 5
    }
}
