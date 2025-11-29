@file:Suppress("TooManyFunctions", "MagicNumber")

package com.spectra.logger.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.spectra.logger.SpectraLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// MARK: - Reusable Components

/**
 * Reusable log-generating button component
 */
@Composable
fun LogButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    action: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { action() },
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.1f),
            ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = backgroundColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Reusable branding card component
 */
@Composable
fun BrandingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Section header for grouping UI elements
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

// MARK: - Utility Functions

/**
 * Share text content using native Android share sheet
 */
fun shareText(context: Context, text: String, title: String = "Share Logs") {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, title))
}

/**
 * Simulates a network request and logs it to the network logs section
 */
suspend fun simulateNetworkRequest(
    method: String,
    url: String,
    statusCode: Int,
    duration: Double,
) {
    // Network request simulation - logs to network storage, not application logs
    val requestHeaders = mapOf(
        "Content-Type" to "application/json",
        "User-Agent" to "SpectraExample/1.0",
        "Accept" to "application/json",
        "Authorization" to "Bearer token_example_12345"
    )

    val responseHeaders = mutableMapOf(
        "Content-Type" to "application/json",
        "Server" to "Example/1.0",
        "X-Response-Time" to "${(duration * 1000).toLong()}ms"
    )

    // Add status-specific headers
    if (statusCode in 200..299) {
        responseHeaders["Cache-Control"] = "max-age=3600"
        responseHeaders["ETag"] = "\"abc123\""
    } else if (statusCode >= 400) {
        responseHeaders["Cache-Control"] = "no-cache, no-store"
        if (statusCode == 429) {
            responseHeaders["Retry-After"] = "60"
        }
    }

    // Generate response body based on status code
    val responseBody = when (statusCode) {
        200 -> "{\"success\": true, \"data\": {\"id\": \"123\", \"message\": \"Request completed successfully\"}}"
        201 -> "{\"success\": true, \"id\": \"newly-created-id\", \"message\": \"Resource created\"}"
        400 -> "{\"error\": \"Bad Request\", \"details\": \"Invalid request parameters\", \"code\": \"INVALID_PARAMS\"}"
        404 -> "{\"error\": \"Not Found\", \"details\": \"The requested resource does not exist\", \"code\": \"RESOURCE_NOT_FOUND\"}"
        500 -> "{\"error\": \"Internal Server Error\", \"details\": \"An unexpected error occurred\", \"code\": \"INTERNAL_ERROR\", \"requestId\": \"req-${java.util.UUID.randomUUID()}\"}"
        else -> "{\"error\": \"HTTP $statusCode\", \"message\": \"Request failed with status code $statusCode\"}"
    }

    val durationMs = (duration * 1000).toLong()

    val networkLogEntry = com.spectra.logger.domain.model.NetworkLogEntry(
        id = java.util.UUID.randomUUID().toString(),
        timestamp = kotlinx.datetime.Clock.System.now(),
        url = url,
        method = method,
        requestHeaders = requestHeaders,
        requestBody = null,
        responseCode = statusCode,
        responseHeaders = responseHeaders,
        responseBody = responseBody,
        duration = durationMs,
        error = if (statusCode >= 400) "HTTP $statusCode: Request failed" else null
    )

    // Add the network log entry to Spectra Logger's network storage
    SpectraLogger.networkStorage.add(networkLogEntry)
}

/**
 * Generates a mock stack trace for demonstrating error logging
 */
fun generateStackTrace(): String {
    return """
        Fatal error: Attempted to divide by zero
        Stack trace:
        0 SpectraExample                    0x0000000104b8e3a0 calculateDivision(_:) + 52
        1 SpectraExample                    0x0000000104b8e2c8 processUserInput(_:) + 120
        2 SpectraExample                    0x0000000104b8e1f0 handleButtonTap() + 88
        3 UIKitCore                         0x00000001a01c9e20 -[UIApplication sendAction:to:from:forEvent:] + 96
        4 UIKitCore                         0x00000001a01c9c00 -[UIControl sendAction:withEvent:] + 128
        5 UIKitCore                         0x00000001a01ca020 -[UIControl _sendActionsForEvents:withEvent:] + 324
        6 SwiftUI                           0x00000001a8c4f188 <closure>() + 420
        7 SwiftUI                           0x00000001a8c4e5d8 closure #1 in _EnvironmentKeyWritingModifier.body + 360
        8 CoreFoundation                    0x000000019fb3e504 __CFRUNLOOP_IS_CALLING_OUT_TO_A_BLOCK__ + 24
        9 CoreFoundation                    0x000000019fb3e050 __CFRunLoopDoBlocks + 368
        10 CoreFoundation                   0x000000019fb3c9e0 __CFRunLoopRun + 828
        11 CoreFoundation                   0x000000019fb3c400 CFRunLoopRunSpecific + 600
        12 GraphicsServices                 0x00000001a4a96050 GSEventRunModal + 164
        13 UIKitCore                        0x00000001a01b1de8 -[UIApplication _run] + 888
        14 UIKitCore                        0x00000001a01b1268 UIApplicationMain + 340
        15 SpectraExample                   0x0000000104b8e000 main + 8
        16 dyld                             0x00000001a01b9e94 start + 2220
    """.trimIndent()
}

// MARK: - Tab: Example Actions

/**
 * Tab showing basic logging examples
 */
@Composable
fun ExampleActionsTab(onOpenSpectra: () -> Unit) {
    var counter by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            LogButton(
                label = "Tap Me (Generates Log)",
                icon = Icons.Default.CheckCircle,
                backgroundColor = Color.Blue,
                action = {
                    counter++
                    SpectraLogger.i("Example", "Button tapped $counter times")
                },
            )
        }

        item {
            LogButton(
                label = "Generate Warning",
                icon = Icons.Default.CheckCircle,
                backgroundColor = Color(0xFFFFA500), // Orange
                action = {
                    SpectraLogger.w("Example", "Warning log generated")
                },
            )
        }

        item {
            LogButton(
                label = "Generate Error",
                icon = Icons.Default.CheckCircle,
                backgroundColor = Color.Red,
                action = {
                    SpectraLogger.e("Example", "Error log generated")
                },
            )
        }

        item {
            LogButton(
                label = "Error with Stack Trace",
                icon = Icons.Default.CheckCircle,
                backgroundColor = Color.Red,
                action = {
                    val stackTrace = generateStackTrace()
                    SpectraLogger.e(
                        "Example",
                        "Fatal error: Attempted to divide by zero",
                        metadata = mapOf(
                            "operation" to "calculateDivision",
                            "dividend" to "10",
                            "divisor" to "0",
                            "severity" to "CRITICAL",
                            "error_type" to "ArithmeticException",
                            "stack_trace" to stackTrace,
                        ),
                    )
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OpenSpectraButton(onOpenSpectra)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// MARK: - Tab: Network Requests

/**
 * Tab showing network request simulation examples
 */
@Composable
fun NetworkRequestsTab(onOpenSpectra: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            BrandingCard(
                icon = Icons.Default.Info,
                title = "Network Testing",
                subtitle = "Simulate HTTP requests and responses",
            )
        }

        item {
            LogButton(
                label = "GET Request (200 OK)",
                icon = Icons.Default.Info,
                backgroundColor = Color.Green,
                action = {
                    coroutineScope.launch {
                        delay(500) // Simulate network delay
                        simulateNetworkRequest(
                            method = "GET",
                            url = "https://api.example.com/users",
                            statusCode = 200,
                            duration = 0.5,
                        )
                    }
                },
            )
        }

        item {
            LogButton(
                label = "POST Request (201 Created)",
                icon = Icons.Default.Info,
                backgroundColor = Color.Green,
                action = {
                    coroutineScope.launch {
                        delay(1000) // Simulate network delay
                        simulateNetworkRequest(
                            method = "POST",
                            url = "https://api.example.com/users",
                            statusCode = 201,
                            duration = 1.0,
                        )
                    }
                },
            )
        }

        item {
            LogButton(
                label = "GET Request (404 Not Found)",
                icon = Icons.Default.Info,
                backgroundColor = Color(0xFFFFA500), // Orange
                action = {
                    coroutineScope.launch {
                        delay(300) // Simulate network delay
                        simulateNetworkRequest(
                            method = "GET",
                            url = "https://api.example.com/users/9999",
                            statusCode = 404,
                            duration = 0.3,
                        )
                    }
                },
            )
        }

        item {
            LogButton(
                label = "Server Error (500)",
                icon = Icons.Default.Info,
                backgroundColor = Color.Red,
                action = {
                    coroutineScope.launch {
                        delay(2000) // Simulate network delay
                        simulateNetworkRequest(
                            method = "GET",
                            url = "https://api.example.com/data",
                            statusCode = 500,
                            duration = 2.0,
                        )
                    }
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OpenSpectraButton(onOpenSpectra)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// MARK: - Open Spectra Button

/**
 * Button to open Spectra Logger modal
 */
@Composable
fun OpenSpectraButton(onClick: () -> Unit) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Open Logger",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Open Spectra Logger",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// MARK: - Main App Screen with Tabs

/**
 * Main app screen with tab-based navigation
 */
@Composable
fun MainAppScreen(onOpenSpectra: () -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs =
        listOf(
            TabItem(
                label = "Actions",
                selectedIcon = Icons.Default.CheckCircle,
                unselectedIcon = Icons.Outlined.CheckCircle,
                contentDescription = "Example Actions",
            ),
            TabItem(
                label = "Network",
                selectedIcon = Icons.Default.Info,
                unselectedIcon = Icons.Outlined.Info,
                contentDescription = "Network Requests",
            ),
        )

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab content
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            when (selectedTabIndex) {
                0 -> ExampleActionsTab(onOpenSpectra)
                1 -> NetworkRequestsTab(onOpenSpectra)
            }
        }

        // Bottom navigation bar
        NavigationBar {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector =
                                if (index == selectedTabIndex) {
                                    tab.selectedIcon
                                } else {
                                    tab.unselectedIcon
                                },
                            contentDescription = tab.contentDescription,
                        )
                    },
                    label = { Text(tab.label) },
                    selected = index == selectedTabIndex,
                    onClick = { selectedTabIndex = index },
                )
            }
        }
    }
}

// MARK: - Spectra Logger Screen

/**
 * Main Spectra Logger screen with tabs for Logs, Network, and Settings
 */
@Composable
fun SpectraLoggerScreen(onClose: () -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isDarkMode by remember { mutableStateOf(false) }

    val tabs = listOf(
        TabItem(
            label = "Logs",
            selectedIcon = Icons.Default.CheckCircle,
            unselectedIcon = Icons.Outlined.CheckCircle,
            contentDescription = "Logs",
        ),
        TabItem(
            label = "Network",
            selectedIcon = Icons.Default.Info,
            unselectedIcon = Icons.Outlined.Info,
            contentDescription = "Network",
        ),
        TabItem(
            label = "Settings",
            selectedIcon = Icons.Default.Info,
            unselectedIcon = Icons.Outlined.Info,
            contentDescription = "Settings",
        ),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Single header with close button
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (selectedTabIndex) {
                    0 -> "Logs"
                    1 -> "Network"
                    2 -> "Settings"
                    else -> "Spectra Logger"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "✕",
                style = MaterialTheme.typography.headlineSmall,
                modifier =
                    Modifier
                        .clickable { onClose() }
                        .padding(8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        HorizontalDivider()

        // Tab content
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            when (selectedTabIndex) {
                0 -> SpectraLogsTab()
                1 -> SpectraNetworkTab()
                2 -> SpectraSettingsTab(onDarkModeChanged = { isDarkMode = it })
            }
        }

        // Single bottom navigation bar
        NavigationBar {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector =
                                if (index == selectedTabIndex) {
                                    tab.selectedIcon
                                } else {
                                    tab.unselectedIcon
                                },
                            contentDescription = tab.contentDescription,
                        )
                    },
                    label = { Text(tab.label) },
                    selected = index == selectedTabIndex,
                    onClick = { selectedTabIndex = index },
                )
            }
        }
    }
}

/**
 * Logs tab with search and filtering
 */
@Composable
fun SpectraLogsTab() {
    var logs by remember { mutableStateOf<List<com.spectra.logger.domain.model.LogEntry>>(emptyList()) }
    var filteredLogs by remember { mutableStateOf<List<com.spectra.logger.domain.model.LogEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }
    var selectedLevels by remember { mutableStateOf<Set<com.spectra.logger.domain.model.LogLevel>>(emptySet()) }
    var availableTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load logs on composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            logs = SpectraLogger.query()
            availableTags = logs.map { it.tag }.distinct().sorted()
            filteredLogs = logs
            isLoading = false
        }
    }

    // Update filtered logs when search or filters change
    LaunchedEffect(searchText, selectedLevels, selectedTags, logs) {
        filteredLogs = logs.filter { log ->
            val matchesSearch = searchText.isBlank() ||
                log.message.contains(searchText, ignoreCase = true) ||
                log.tag.contains(searchText, ignoreCase = true)

            val matchesLevel = selectedLevels.isEmpty() || log.level in selectedLevels
            val matchesTag = selectedTags.isEmpty() || log.tag in selectedTags

            matchesSearch && matchesLevel && matchesTag
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar with share button
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier =
                    Modifier
                        .weight(1f),
                placeholder = { Text("Search logs...") },
                singleLine = true,
            )
            Button(
                onClick = {
                    val logsText = filteredLogs.map { log ->
                        "[${formatTimestamp(log.timestamp)}] ${log.level.name} - ${log.tag}: ${log.message}"
                    }.joinToString("\n")
                    shareText(context, logsText.ifEmpty { "No logs to share" }, "Share Logs")
                },
                modifier = Modifier.padding(4.dp),
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Share", modifier = Modifier.size(20.dp))
            }
        }

        // Log Level Filter Chips
        if (com.spectra.logger.domain.model.LogLevel.entries.isNotEmpty()) {
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(com.spectra.logger.domain.model.LogLevel.entries.size) { index ->
                    val level = com.spectra.logger.domain.model.LogLevel.entries[index]
                    val isSelected = level in selectedLevels

                    Card(
                        modifier =
                            Modifier
                                .clickable {
                                    selectedLevels =
                                        if (isSelected) {
                                            selectedLevels - level
                                        } else {
                                            selectedLevels + level
                                        }
                                }
                                .padding(4.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                            ),
                    ) {
                        Text(
                            text = level.name,
                            modifier = Modifier.padding(8.dp),
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }
        }

        // Tag Filter Chips
        if (availableTags.isNotEmpty()) {
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(availableTags.size) { index ->
                    val tag = availableTags[index]
                    val isSelected = tag in selectedTags

                    Card(
                        modifier =
                            Modifier
                                .clickable {
                                    selectedTags =
                                        if (isSelected) {
                                            selectedTags - tag
                                        } else {
                                            selectedTags + tag
                                        }
                                }
                                .padding(4.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                            ),
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(8.dp),
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // Logs list
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading logs...")
            }
        } else if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "No logs to display",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        } else if (filteredLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "No matching logs",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(filteredLogs.size) { index ->
                    val log = filteredLogs[index]
                    LogEntryItem(log)
                }
            }
        }
    }
}

/**
 * Network logs tab
 */
@Composable
fun SpectraNetworkTab() {
    var networkLogs by remember { mutableStateOf<List<com.spectra.logger.domain.model.NetworkLogEntry>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            networkLogs = SpectraLogger.queryNetwork()
            isLoading = false
        }
    }

    val filteredLogs = remember(networkLogs, searchText) {
        networkLogs.filter { log ->
            searchText.isBlank() ||
                log.url.contains(searchText, ignoreCase = true) ||
                log.method.contains(searchText, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar with share button
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search network logs...") },
                singleLine = true,
            )
            Button(
                onClick = {
                    val logsText = filteredLogs.map { log ->
                        "[${formatTimestamp(log.timestamp)}] ${log.method} ${log.responseCode} - ${log.url}"
                    }.joinToString("\n")
                    shareText(context, logsText.ifEmpty { "No network logs to share" }, "Share Network Logs")
                },
                modifier = Modifier.padding(4.dp),
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Share", modifier = Modifier.size(20.dp))
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading network logs...")
            }
        } else if (networkLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "No network logs",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        } else if (filteredLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "No matching network logs",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(filteredLogs.size) { index ->
                    val networkLog = filteredLogs[index]
                    NetworkLogEntryItem(networkLog)
                }
            }
        }
    }
}

/**
 * Settings tab with appearance, storage, and export options
 */
@Composable
fun SpectraSettingsTab(onDarkModeChanged: (Boolean) -> Unit = {}) {
    var logCount by remember { mutableStateOf(0) }
    var networkLogCount by remember { mutableStateOf(0) }
    var appearanceMode by remember { mutableStateOf("System") }
    var showClearLogsDialog by remember { mutableStateOf(false) }
    var showClearNetworkDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val version = remember { SpectraLogger.getVersion() }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            logCount = SpectraLogger.count()
            networkLogCount = SpectraLogger.networkCount()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        // Appearance Section
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose how Spectra Logger appears",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Light", "Dark", "System").forEach { mode ->
                        Card(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clickable {
                                        appearanceMode = mode
                                        if (mode == "Dark") {
                                            onDarkModeChanged(true)
                                        } else {
                                            onDarkModeChanged(false)
                                        }
                                    },
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        if (appearanceMode == mode) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                ),
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    if (appearanceMode == mode) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }
                }
            }
        }

        // Storage Section
        Text(
            text = "Storage",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Application Logs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Application Logs",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "$logCount logs stored",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = { showClearLogsDialog = true },
                        enabled = logCount > 0,
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    ) {
                        Text("Clear", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider()

                // Network Logs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Network Logs",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "$networkLogCount logs stored",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = { showClearNetworkDialog = true },
                        enabled = networkLogCount > 0,
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    ) {
                        Text("Clear", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // About Section
        Text(
            text = "About",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = version,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Framework",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Spectra Logger",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Clear logs confirmation dialog
    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            title = { Text("Clear Application Logs?") },
            text = { Text("This will permanently delete all $logCount application logs.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SpectraLogger.clear()
                            logCount = 0
                            showClearLogsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showClearLogsDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Clear network logs confirmation dialog
    if (showClearNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showClearNetworkDialog = false },
            title = { Text("Clear Network Logs?") },
            text = { Text("This will permanently delete all $networkLogCount network logs.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SpectraLogger.clearNetwork()
                            networkLogCount = 0
                            showClearNetworkDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showClearNetworkDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Individual network log entry item
 */
@Composable
fun NetworkLogEntryItem(networkLog: com.spectra.logger.domain.model.NetworkLogEntry) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = networkLog.method,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = networkLog.responseCode?.toString() ?: "?",
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        when (val code = networkLog.responseCode?.toInt()) {
                            in 200..299 -> Color.Green
                            in 300..399 -> Color.Blue
                            in 400..499 -> Color(0xFFFFA500)
                            in 500..599 -> Color.Red
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
            Text(
                text = networkLog.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
            Text(
                text = "Duration: ${networkLog.duration}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Individual log entry item in the logger
 */
@Composable
fun LogEntryItem(log: com.spectra.logger.domain.model.LogEntry) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (log.level) {
                        com.spectra.logger.domain.model.LogLevel.VERBOSE ->
                            Color.Gray.copy(alpha = 0.1f)
                        com.spectra.logger.domain.model.LogLevel.DEBUG ->
                            Color.Blue.copy(alpha = 0.1f)
                        com.spectra.logger.domain.model.LogLevel.INFO ->
                            Color.Green.copy(alpha = 0.1f)
                        com.spectra.logger.domain.model.LogLevel.WARNING ->
                            Color(0xFFFFA500).copy(alpha = 0.1f)
                        com.spectra.logger.domain.model.LogLevel.ERROR ->
                            Color.Red.copy(alpha = 0.1f)
                        com.spectra.logger.domain.model.LogLevel.FATAL ->
                            Color.Red.copy(alpha = 0.2f)
                    },
            ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = log.level.name,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        when (log.level) {
                            com.spectra.logger.domain.model.LogLevel.VERBOSE -> Color.Gray
                            com.spectra.logger.domain.model.LogLevel.DEBUG -> Color.Blue
                            com.spectra.logger.domain.model.LogLevel.INFO -> Color.Green
                            com.spectra.logger.domain.model.LogLevel.WARNING -> Color(0xFFFFA500)
                            com.spectra.logger.domain.model.LogLevel.ERROR -> Color.Red
                            com.spectra.logger.domain.model.LogLevel.FATAL -> Color.Red
                        },
                )
            }
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = formatTimestamp(log.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Format timestamp for display
 */
private fun formatTimestamp(instant: kotlinx.datetime.Instant): String {
    return instant.toString().substringAfter("T").take(12)
}

// MARK: - Data Classes

/**
 * Data class for navigation tab items
 */
private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
)
