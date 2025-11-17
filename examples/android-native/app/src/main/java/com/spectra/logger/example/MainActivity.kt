@file:Suppress("TooManyFunctions", "MagicNumber")

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Network tab screen - shows network requests with search and filters
 * Aligned with iOS SwiftUI implementation
 */
@Composable
fun NetworkTabScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedStatuses by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        NetworkSearchBar(searchText) { searchText = it }
        NetworkFilterChips(selectedStatuses) { status, isSelected ->
            selectedStatuses =
                if (isSelected) {
                    selectedStatuses + status
                } else {
                    selectedStatuses - status
                }
        }
        NetworkRequestsList()
    }
}

/**
 * Search bar for network requests
 */
@Composable
fun NetworkSearchBar(
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
        placeholder = { Text("Search requests...") },
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
 * Filter chips for HTTP status codes
 */
@Composable
fun NetworkFilterChips(
    selectedStatuses: Set<String>,
    onStatusChange: (String, Boolean) -> Unit,
) {
    val statuses = listOf("2xx", "3xx", "4xx", "5xx")
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        statuses.forEach { status ->
            FilterChip(
                label = status,
                isSelected = selectedStatuses.contains(status),
                onSelectionChange = { isSelected ->
                    onStatusChange(status, isSelected)
                },
            )
        }
    }
}

/**
 * List of network requests
 */
@Composable
fun NetworkRequestsList() {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(count = 5) { index -> NetworkRequestCard(index) }
    }
}

/**
 * Network request card with method, URL, and status
 */
@Composable
fun NetworkRequestCard(index: Int) {
    val methods = listOf("GET", "POST", "PUT", "DELETE")
    val currentMethod = methods[index % methods.size]
    val methodColor = getColorForMethod(currentMethod)

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
            NetworkRequestHeader(currentMethod, methodColor)
            NetworkRequestDetails(index)
        }
    }
}

/**
 * Header row with method, status code, and duration
 */
@Composable
fun NetworkRequestHeader(
    method: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MethodBadge(method, color)
        NetworkRequestMeta()
    }
}

/**
 * HTTP method badge
 */
@Composable
fun MethodBadge(
    method: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier =
            Modifier
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = method,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

/**
 * Status code and duration metadata
 */
@Composable
fun NetworkRequestMeta() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "200 OK",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.6f),
        )
        Text(
            text = "234ms",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Network request URL and details
 */
@Composable
fun NetworkRequestDetails(index: Int) {
    Text(
        text = "/api/v1/endpoint?param=value&id=$index",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        modifier = Modifier.padding(top = 8.dp),
    )
}

/**
 * Get color for HTTP method
 */
fun getColorForMethod(method: String): androidx.compose.ui.graphics.Color {
    val methodColors =
        mapOf(
            "GET" to 0xFF2196F3,
            "POST" to 0xFF4CAF50,
            "PUT" to 0xFFFF9800,
            "DELETE" to 0xFFF44336,
            "PATCH" to 0xFF9C27B0,
        )
    return androidx.compose.ui.graphics.Color(methodColors[method] ?: 0xFF2196F3)
}

/**
 * Settings tab screen - configuration and storage management
 * Aligned with iOS SwiftUI implementation
 */
@Composable
fun SettingsTabScreen() {
    var darkModeEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
    ) {
        item { SettingsSectionTitle("Appearance") }
        item {
            SettingRow(
                title = "Dark Mode",
                description = "Toggle dark theme",
                isEnabled = darkModeEnabled,
                onToggle = { darkModeEnabled = !darkModeEnabled },
            )
        }

        item { SettingsSectionTitle("Storage") }
        item { StorageInfoCard() }
        item {
            SettingActionButton(
                label = "Clear All Logs",
                onClick = {},
                isDestructive = true,
            )
        }
        item {
            SettingActionButton(
                label = "Export Logs",
                onClick = {},
                isDestructive = false,
            )
        }

        item { SettingsSectionTitle("About") }
        item {
            InfoRow(label = "Version", value = "1.0.0")
        }
        item {
            InfoRow(label = "Build", value = "1")
        }
    }
}

/**
 * Settings section title
 */
@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

/**
 * Setting row with toggle
 */
@Suppress("LongMethod")
@Composable
fun SettingRow(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onToggle() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth(0.7f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                modifier =
                    Modifier
                        .background(
                            if (isEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            RoundedCornerShape(12.dp),
                        )
                        .padding(4.dp),
                color =
                    if (isEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp, 28.dp),
                )
            }
        }
    }
}

/**
 * Storage information card
 */
@Composable
fun StorageInfoCard() {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Log Storage",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "1.2 MB / 10 MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StorageProgressBar(current = 1.2f, total = 10f)
        }
    }
}

/**
 * Storage progress bar
 */
@Composable
fun StorageProgressBar(
    current: Float,
    total: Float,
) {
    val progress = current / total
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp),
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp),
                    ),
        )
    }
}

/**
 * Action button for destructive and normal actions
 */
@Composable
fun SettingActionButton(
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isDestructive) {
                        androidx.compose.ui.graphics.Color(0xFFF44336).copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (isDestructive) {
                    androidx.compose.ui.graphics.Color(0xFFF44336)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            modifier = Modifier.padding(12.dp),
        )
    }
}

/**
 * Information row with label and value
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Tag$index",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.6f),
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
