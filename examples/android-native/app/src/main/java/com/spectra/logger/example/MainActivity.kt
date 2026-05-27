package com.spectra.logger.example

import com.spectra.logger.core.ui.SpectraUI
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Example activity demonstrating Spectra Logger usage.
 * Shows a simple interface with tabs for Actions and Network requests.
 * Includes an "Open Spectra Logger" button to view the captured logs.
 */
class MainActivity : ComponentActivity() {
    private val showSpectraLogger = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        configureLogger()
        seedHistoricalLogs()
        generateSampleLogs()

        setContent {
            MaterialTheme {
                com.spectra.logger.core.ui.compose.SpectraLoggerFabOverlay {
                    MainAppScreen(onOpenSpectra = { SpectraUI.showScreen() })
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

    private fun seedHistoricalLogs() {
        lifecycleScope.launch {
            val now = SpectraTime.now()
            val logs = mutableListOf<LogEntry>()

            val tags = listOf("Auth", "Network", "Database", "UI", "Storage", "Lifecycle")
            val messages = mapOf(
                LogLevel.VERBOSE to listOf(
                    "Cached user profile lookup",
                    "UI recomposition triggered for view log list",
                    "Cursor moved to row 45 in database query",
                    "Button focus gained",
                    "Shared preferences read key: dark_mode"
                ),
                LogLevel.DEBUG to listOf(
                    "Auth token validation started",
                    "Database connection pool size: 8 active, 2 idle",
                    "Network request response body deserialized in 12ms",
                    "Cache hit for key 'session_token'",
                    "Activity state change: onSaveInstanceState"
                ),
                LogLevel.INFO to listOf(
                    "User profile fetched successfully",
                    "Network request POST /api/v1/auth/login returned HTTP 200",
                    "Local database migrated to version 4",
                    "File uploaded successfully: log_export_923.txt",
                    "User settings updated: notifications=enabled"
                ),
                LogLevel.WARNING to listOf(
                    "Slow database query detected: SELECT * FROM logs WHERE ... took 450ms",
                    "Network request retrying due to timeout (attempt 1/3)",
                    "High memory consumption: 80% JVM heap utilized",
                    "Disk space running low: 2.1GB remaining",
                    "API returned deprecated header 'x-deprecated-auth'"
                ),
                LogLevel.ERROR to listOf(
                    "Failed to connect to authentication server: ConnectException",
                    "Write failed in local SQLite: Disk is full or database is locked",
                    "Network request POST /api/v1/user/update failed with HTTP 500",
                    "JSON parsing error: missing field 'user_id' in response",
                    "Failed to write exported logs file: Permission denied"
                ),
                LogLevel.FATAL to listOf(
                    "Uncaught exception: NullPointerException at line 142 in UserManager",
                    "Fatal application crash: OutOfMemoryError in Bitmap allocation",
                    "Database corruption detected: master table is unreadable"
                )
            )

            // Seed logs spanning the last 15 minutes
            for (minutesAgo in 0..15) {
                val bucketTime = now - minutesAgo.minutes

                // Determine counts based on level to get a beautiful, highly realistic distribution
                // Verbose, Debug, Info have higher counts. Warning, Error, Fatal have much lower.
                val counts = mapOf(
                    LogLevel.VERBOSE to (5..12).random(),
                    LogLevel.DEBUG to (8..15).random(),
                    LogLevel.INFO to (10..25).random(),
                    LogLevel.WARNING to (1..4).random(),
                    LogLevel.ERROR to if (minutesAgo % 3 == 0) (1..2).random() else 0,
                    LogLevel.FATAL to if (minutesAgo == 4 || minutesAgo == 11) 1 else 0
                )

                counts.forEach { (level, count) ->
                    val levelMsgs = messages[level] ?: emptyList()
                    repeat(count) { i ->
                        // Offset by random seconds to stagger the logs within the minute
                        val offsetSeconds = (0..59).random()
                        val entryTime = bucketTime - offsetSeconds.seconds

                        val tag = tags.random()
                        val message = levelMsgs.random() + " (Staggered #$i)"
                        val throwable = if (level >= LogLevel.ERROR) {
                            "Exception: Simulated ${level.name} at com.spectra.logger.example.seed"
                        } else null

                        logs.add(
                            LogEntry(
                                id = IdGenerator.generate(),
                                timestamp = entryTime,
                                level = level,
                                tag = tag,
                                message = message,
                                throwable = throwable,
                                source = "example-app",
                                sourceType = SourceType.APP
                            )
                        )
                    }
                }
            }

            // Write all to storage in one batch
            SpectraLogger.logStorage.addAll(logs)
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
