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
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
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
 * Shows a simple interface with tabs for Actions, Network requests, and Events.
 * Includes an "Open Spectra Logger" button to view the captured logs.
 */
class MainActivity : ComponentActivity() {
    private val showSpectraLogger = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        configureLogger()
        seedHistoricalLogs()
        seedHistoricalEvents()
        generateSampleLogs()
        generateSampleEvents()

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
            eventStorage {
                maxCapacity = MAX_EVENT_STORAGE_CAPACITY
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

    private fun seedHistoricalEvents() {
        lifecycleScope.launch {
            val now = SpectraTime.now()
            val events = mutableListOf<EventLogEntry>()

            val screenNames = listOf("HomeScreen", "CatalogScreen", "ProductDetailsScreen", "SettingsScreen", "ProfileScreen")
            val userActions = listOf(
                Pair("button_tap", mapOf("target" to "explore_more")),
                Pair("filter_applied", mapOf("category" to "audio", "brand" to "Sony")),
                Pair("add_to_cart", mapOf("item_id" to "SKU-9901", "price" to "149.99")),
                Pair("search_executed", mapOf("query" to "noise cancelling", "results" to "8")),
                Pair("item_favorited", mapOf("item_id" to "SKU-9901"))
            )
            val lifecycles = listOf(
                Pair("app_foreground", mapOf("cold_start" to "true")),
                Pair("screen_orientation_change", mapOf("orientation" to "landscape")),
                Pair("network_state_change", mapOf("network" to "wifi")),
                Pair("session_renewed", mapOf("session_id" to "sess-84920"))
            )
            val customEvents = listOf(
                Pair("promo_code_applied", mapOf("code" to "SUMMER2026", "discount" to "15%")),
                Pair("checkout_completed", mapOf("order_id" to "ORD-7712", "amount" to "149.99")),
                Pair("review_submitted", mapOf("rating" to "5", "has_text" to "true")),
                Pair("feature_flag_evaluation", mapOf("flag" to "dark_mode_v2", "variant" to "enabled"))
            )

            // Seed historical events spanning the last 15 minutes
            for (minutesAgo in 0..15) {
                val bucketTime = now - minutesAgo.minutes

                // Seed screen views
                if (minutesAgo % 2 == 0) {
                    val screen = screenNames.random()
                    val duration = (1200L..45000L).random()
                    events.add(
                        EventLogEntry(
                            id = IdGenerator.generate(),
                            timestamp = bucketTime - (0..30).random().seconds,
                            eventType = EventType.SCREEN_VIEW,
                            name = screen,
                            parameters = mapOf("screen_class" to screen, "platform" to "Android"),
                            durationMs = duration,
                            source = "example-app",
                            sourceType = SourceType.APP
                        )
                    )
                }

                // Seed user actions
                repeat((1..2).random()) {
                    val action = userActions.random()
                    events.add(
                        EventLogEntry(
                            id = IdGenerator.generate(),
                            timestamp = bucketTime - (10..55).random().seconds,
                            eventType = EventType.USER_ACTION,
                            name = action.first,
                            parameters = action.second,
                            source = "example-app",
                            sourceType = SourceType.APP
                        )
                    )
                }

                // Seed lifecycle events
                if (minutesAgo % 3 == 0) {
                    val lifecycle = lifecycles.random()
                    events.add(
                        EventLogEntry(
                            id = IdGenerator.generate(),
                            timestamp = bucketTime - (5..45).random().seconds,
                            eventType = EventType.LIFECYCLE,
                            name = lifecycle.first,
                            parameters = lifecycle.second,
                            source = "example-app",
                            sourceType = SourceType.APP
                        )
                    )
                }

                // Seed custom business events
                if (minutesAgo % 4 == 0) {
                    val custom = customEvents.random()
                    events.add(
                        EventLogEntry(
                            id = IdGenerator.generate(),
                            timestamp = bucketTime - (15..50).random().seconds,
                            eventType = EventType.CUSTOM,
                            name = custom.first,
                            parameters = custom.second,
                            source = "example-app",
                            sourceType = SourceType.APP
                        )
                    )
                }
            }

            SpectraLogger.eventStorage.addAll(events)
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

    private fun generateSampleEvents() {
        lifecycleScope.launch {
            delay(DELAY_SHORT)
            SpectraLogger.event(
                name = "app_launch",
                parameters = mapOf("version" to SpectraLogger.getVersion(), "build_type" to "debug"),
                eventType = EventType.LIFECYCLE
            )
            SpectraLogger.event(
                name = "session_started",
                parameters = mapOf("user_type" to "standard_user"),
                eventType = EventType.CUSTOM
            )
        }
    }

    private companion object {
        private const val MAX_LOG_STORAGE_CAPACITY = 20_000
        private const val MAX_EVENT_STORAGE_CAPACITY = 5_000
        private const val DELAY_SHORT = 500L
        private const val DELAY_MEDIUM = 1000L
    }
}
