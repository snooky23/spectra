package com.spectra.logger.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
 * Logs tab screen - shows application logs with search and filters
 * Aligned with iOS SwiftUI implementation
 */
@Composable
fun LogsTabScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedLevels by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        LogsSearchBar(searchText) { searchText = it }
        LogsFilterChips(selectedLevels) { level, isSelected ->
            selectedLevels =
                if (isSelected) {
                    selectedLevels + level
                } else {
                    selectedLevels - level
                }
        }
        LogsList()
    }
}

/**
 * Search bar for logs with clear button
 */
@Composable
fun LogsSearchBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
) {
    TextField(
        value = searchText,
        onValueChange = onSearchChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search logs...") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search")
        },
        trailingIcon =
            if (searchText.isNotEmpty()) {
                {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.clickable { onSearchChange("") },
                    )
                }
            } else {
                null
            },
        singleLine = true,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        shape = RoundedCornerShape(8.dp),
    )
}

/**
 * Horizontal scrollable filter chips for log levels
 */
@Composable
fun LogsFilterChips(
    selectedLevels: Set<String>,
    onLevelChange: (String, Boolean) -> Unit,
) {
    val logLevels = listOf("VERBOSE", "DEBUG", "INFO", "WARNING", "ERROR", "FATAL")
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        logLevels.forEach { level ->
            FilterChip(
                label = level,
                isSelected = selectedLevels.contains(level),
                onSelectionChange = { isSelected -> onLevelChange(level, isSelected) },
            )
        }
    }
}

/**
 * Lazy list of log entries
 */
@Composable
fun LogsList() {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(count = 5) { index -> LogEntryCard(index) }
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

/**
 * Reusable filter chip component for log levels and tags
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .clickable { onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(16.dp),
        color =
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/**
 * Log entry card displaying a single log with level badge, tag, and message
 */
@Composable
fun LogEntryCard(index: Int) {
    val levels = listOf("VERBOSE", "DEBUG", "INFO", "WARNING", "ERROR")
    val currentLevel = levels[index % levels.size]
    val currentColor = getColorForLevel(currentLevel)

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            LogEntryHeader(currentLevel, currentColor, index)
            LogEntryMessage(index)
        }
    }
}

/**
 * Header row with level badge, tag, and timestamp
 */
@Composable
fun LogEntryHeader(
    level: String,
    color: androidx.compose.ui.graphics.Color,
    index: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LevelBadge(level, color)
        LogEntryMetadata(index)
    }
}

/**
 * Level badge component
 */
@Composable
fun LevelBadge(
    level: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier =
            Modifier
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = level,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

/**
 * Tag and timestamp metadata
 */
@Composable
fun LogEntryMetadata(index: Int) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Tag$index",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "12:30 PM",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Log message text
 */
@Composable
fun LogEntryMessage(index: Int) {
    Text(
        text = "Sample log message $index - This is a longer log message for demonstration",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        modifier = Modifier.padding(top = 8.dp),
    )
}

/**
 * Get color for log level
 */
fun getColorForLevel(level: String): androidx.compose.ui.graphics.Color {
    val levelColors =
        mapOf(
            "VERBOSE" to 0xFF9E9E9E,
            "DEBUG" to 0xFF2196F3,
            "INFO" to 0xFF4CAF50,
            "WARNING" to 0xFFFF9800,
            "ERROR" to 0xFFF44336,
            "FATAL" to 0xFF7C0A02,
        )
    return androidx.compose.ui.graphics.Color(levelColors[level] ?: 0xFF9E9E9E)
}
