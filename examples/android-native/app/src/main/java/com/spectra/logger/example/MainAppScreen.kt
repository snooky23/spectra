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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Simulates a network request and logs it to the network logs section
 */
fun simulateNetworkRequest(
    method: String,
    url: String,
    statusCode: Int,
    duration: Double,
) {
    // Simulate on coroutine scope (this will be called from compose context)
    // Network request simulation - would be called from viewModel in real app
    // For now, just log the network event
    SpectraLogger.i(
        "NetworkRequest",
        "$method $url - Status: $statusCode (${duration * 1000}ms)",
        metadata = mapOf(
            "method" to method,
            "url" to url,
            "statusCode" to statusCode.toString(),
            "duration_ms" to (duration * 1000).toLong().toString(),
        ),
    )
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            BrandingCard(
                icon = Icons.Default.CheckCircle,
                title = "Example App",
                subtitle = "with Spectra Logger Integration",
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            SectionHeader("Example Actions")
        }

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
            Spacer(modifier = Modifier.height(20.dp))
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            BrandingCard(
                icon = Icons.Default.Info,
                title = "Network Testing",
                subtitle = "Simulate HTTP requests and responses",
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            SectionHeader("Network Requests")
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
            Spacer(modifier = Modifier.height(20.dp))
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
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
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTabIndex) {
                0 -> ExampleActionsTab(onOpenSpectra)
                1 -> NetworkRequestsTab(onOpenSpectra)
            }
        }
    }
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
