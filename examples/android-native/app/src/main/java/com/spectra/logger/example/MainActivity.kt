package com.spectra.logger.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Example activity demonstrating Spectra Logger usage with aligned UI.
 * Shows a unified interface with tabs for Logs, Network, and Settings.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureLogger()

        // Generate sample logs
        generateSampleLogs()

        setContent {
            MaterialTheme {
                SpectraLoggerScreenWithTabs()
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

/**
 * Main Spectra Logger screen with tab navigation matching iOS TabView
 */
@Composable
fun SpectraLoggerScreenWithTabs() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            SpectraNavigationBar(
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            when (selectedTabIndex) {
                0 -> LogsTabScreen()
                1 -> NetworkTabScreen()
                2 -> SettingsTabScreen()
            }
        }
    }
}

/**
 * Navigation bar for Spectra Logger screens
 */
@Composable
fun SpectraNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs =
        listOf(
            TabItem(
                label = "Logs",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                contentDescription = "Application Logs",
            ),
            TabItem(
                label = "Network",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                contentDescription = "Network Requests",
            ),
            TabItem(
                label = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                contentDescription = "Settings",
            ),
        )

    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector =
                            if (index == selectedIndex) {
                                tab.selectedIcon
                            } else {
                                tab.unselectedIcon
                            },
                        contentDescription = tab.contentDescription,
                    )
                },
                label = { Text(tab.label) },
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
            )
        }
    }
}

/**
 * Data class for navigation tab items
 */
private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
)

/**
 * Logs tab screen - shows application logs
 * Aligned with iOS SwiftUI implementation
 */
@Composable
fun LogsTabScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "Logs",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Application logs are captured and displayed here. " +
                    "Tabs are aligned with iOS SwiftUI implementation.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Network tab screen - shows network requests
 * Aligned with iOS SwiftUI implementation
 */
@Composable
fun NetworkTabScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "Network",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Network requests are logged and displayed here. " +
                    "Features are aligned with iOS.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Settings tab screen - configuration and storage management
 * Aligned with iOS SwiftUI implementation
 */
@Composable
fun SettingsTabScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "App configuration and log storage management. " +
                    "UI structure matches iOS SwiftUI.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
