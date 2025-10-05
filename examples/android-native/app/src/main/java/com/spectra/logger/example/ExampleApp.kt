package com.spectra.logger.example

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spectra.logger.SpectraLogger
import com.spectra.logger.ui.screens.LogListScreen
import com.spectra.logger.ui.screens.NetworkLogScreen
import com.spectra.logger.ui.screens.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports all logs to a text file and opens the system share dialog.
 */
private suspend fun exportLogs(context: Context) =
    withContext(Dispatchers.IO) {
        try {
            // Get all logs
            val logs = SpectraLogger.logStorage.query()
            val networkLogs = SpectraLogger.networkStorage.query()

            // Create export content
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val content = buildString {
                appendLine("Spectra Logger Export")
                appendLine("Generated: $timestamp")
                appendLine("=" + "=".repeat(SEPARATOR_LENGTH))
                appendLine()

                appendLine("APPLICATION LOGS (${logs.size})")
                appendLine("-" + "-".repeat(SEPARATOR_LENGTH))
                logs.forEach { log ->
                    appendLine("[${log.level}] ${log.timestamp} - ${log.tag}")
                    appendLine("  ${log.message}")
                    log.metadata.takeIf { it.isNotEmpty() }?.let { metadata ->
                        appendLine("  Metadata: $metadata")
                    }
                    log.throwable?.let { throwable ->
                        appendLine("  Exception: $throwable")
                    }
                    appendLine()
                }

                appendLine()
                appendLine("NETWORK LOGS (${networkLogs.size})")
                appendLine("-" + "-".repeat(SEPARATOR_LENGTH))
                networkLogs.forEach { log ->
                    appendLine("[${log.method}] ${log.url}")
                    appendLine("  Timestamp: ${log.timestamp}")
                    appendLine("  Duration: ${log.duration}ms")
                    log.responseCode?.let { code ->
                        appendLine("  Status: $code")
                    }
                    log.requestHeaders.takeIf { it.isNotEmpty() }?.let { headers ->
                        appendLine("  Request Headers: $headers")
                    }
                    log.requestBody?.let { body ->
                        appendLine("  Request Body: $body")
                    }
                    log.responseHeaders.takeIf { it.isNotEmpty() }?.let { headers ->
                        appendLine("  Response Headers: $headers")
                    }
                    log.responseBody?.let { body ->
                        appendLine("  Response Body: $body")
                    }
                    log.error?.let { error ->
                        appendLine("  Error: $error")
                    }
                    appendLine()
                }
            }

            // Write to file
            val file = File(context.cacheDir, "spectra_logs_$timestamp.txt")
            file.writeText(content)

            // Create share intent with FileProvider
            withContext(Dispatchers.Main) {
                val uri =
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.spectra.fileprovider",
                        file,
                    )

                val shareIntent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Spectra Logger Export")
                        putExtra(Intent.EXTRA_TEXT, "Logs exported from Spectra Logger")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                context.startActivity(
                    Intent.createChooser(shareIntent, "Export Logs"),
                )
            }

            SpectraLogger.i("Export", "Successfully exported ${logs.size + networkLogs.size} logs")
        } catch (e: Exception) {
            SpectraLogger.e("Export", "Failed to export logs", e)
        }
    }

private const val SEPARATOR_LENGTH = 80

/**
 * Navigation screen definitions for the example app.
 */
sealed class Screen(val route: String, val title: String) {
    /**
     * Logs screen showing application logs.
     */
    data object Logs : Screen("logs", "Logs")

    /**
     * Network screen showing network request logs.
     */
    data object Network : Screen("network", "Network")

    /**
     * Settings screen for logger configuration.
     */
    data object Settings : Screen("settings", "Settings")
}

/**
 * Main application composable with bottom navigation.
 */
@Composable
fun ExampleApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            scope = scope,
        )
    }
}

@Composable
private fun BottomNavigationBar(navController: androidx.navigation.NavHostController) {
    val items =
        listOf(
            Triple(Screen.Logs, Icons.Default.List, "Logs"),
            Triple(Screen.Network, Icons.Default.Warning, "Network"),
            Triple(Screen.Settings, Icons.Default.Settings, "Settings"),
        )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Logs.route,
        modifier = modifier,
    ) {
        composable(Screen.Logs.route) {
            LogListScreen(
                storage = SpectraLogger.logStorage,
            )
        }

        composable(Screen.Network.route) {
            NetworkLogScreen(
                storage = SpectraLogger.networkStorage,
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                logStorage = SpectraLogger.logStorage,
                networkLogStorage = SpectraLogger.networkStorage,
                currentMinLevel = SpectraLogger.configuration.minLogLevel,
                onMinLevelChange = { newLevel ->
                    SpectraLogger.configure {
                        minLogLevel = newLevel
                    }
                },
                onExportLogs = {
                    scope.launch {
                        exportLogs(context)
                    }
                },
            )
        }
    }
}
