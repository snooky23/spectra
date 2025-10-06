package com.spectra.logger.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spectra.logger.SpectraLogger

/**
 * SpectraLoggerScreen - Complete ready-to-use logger UI with all tabs.
 *
 * This is the main SDK component that users interact with.
 * It provides a complete logger UI with all tabs included - no need to manage
 * individual screens, tabs, or navigation.
 *
 * ## Usage
 *
 * ### Android (Compose)
 * ```kotlin
 * // That's it! Complete logger UI with all tabs
 * SpectraLoggerScreen()
 * ```
 *
 * ### In a Dialog
 * ```kotlin
 * if (showLogger) {
 *     Dialog(onDismissRequest = { showLogger = false }) {
 *         SpectraLoggerScreen()
 *     }
 * }
 * ```
 *
 * ## What's Included Automatically
 * - **Logs Tab**: Application logs with filtering and search
 * - **Network Tab**: Network request logs
 * - **Settings Tab**: Log management and export options
 * - **Bottom Navigation**: Tab bar with live log counts
 * - **Navigation Handling**: All tab switching logic
 *
 * Users don't need to:
 * - Build individual LogListScreen, NetworkLogScreen, or SettingsScreen
 * - Configure navigation or tab bars
 * - Manage tab state or switching
 *
 * @param modifier Modifier to be applied to the root scaffold
 */
@Composable
fun SpectraLoggerScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            SpectraNavigationBar(navController)
        },
        modifier = modifier,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SpectraTab.Logs.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(SpectraTab.Logs.route) {
                LogListScreen(storage = SpectraLogger.logStorage)
            }
            composable(SpectraTab.Network.route) {
                NetworkLogScreen(storage = SpectraLogger.networkStorage)
            }
            composable(SpectraTab.Settings.route) {
                SettingsScreen(
                    logStorage = SpectraLogger.logStorage,
                    networkLogStorage = SpectraLogger.networkStorage,
                )
            }
        }
    }
}

/**
 * Internal navigation bar for Spectra Logger.
 * This is private SDK implementation - users never interact with this directly.
 */
@Composable
private fun SpectraNavigationBar(navController: androidx.navigation.NavHostController) {
    // Track log counts for badges
    val appLogsCount = remember { mutableIntStateOf(0) }
    val networkLogsCount = remember { mutableIntStateOf(0) }

    // Update counts when new logs arrive
    LaunchedEffect(Unit) {
        SpectraLogger.logStorage.observe().collect {
            appLogsCount.intValue = SpectraLogger.logStorage.count()
        }
    }

    LaunchedEffect(Unit) {
        SpectraLogger.networkStorage.observe().collect {
            networkLogsCount.intValue = SpectraLogger.networkStorage.count()
        }
    }

    val tabs =
        listOf(
            SpectraNavItem(SpectraTab.Logs, Icons.AutoMirrored.Filled.List, "Logs", appLogsCount.intValue),
            SpectraNavItem(SpectraTab.Network, Icons.Default.Warning, "Network", networkLogsCount.intValue),
            SpectraNavItem(SpectraTab.Settings, Icons.Default.Settings, "Settings"),
        )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        tabs.forEach { tab ->
            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        if (tab.count != null && tab.count > 0) {
                            "${tab.label} (${tab.count})"
                        } else {
                            tab.label
                        },
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.route == tab.tab.route } == true,
                onClick = {
                    navController.navigate(tab.tab.route) {
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

/**
 * Internal tab definitions - private SDK implementation.
 */
private sealed class SpectraTab(val route: String) {
    data object Logs : SpectraTab("spectra_logs")
    data object Network : SpectraTab("spectra_network")
    data object Settings : SpectraTab("spectra_settings")
}

/**
 * Internal navigation item - private SDK implementation.
 */
private data class SpectraNavItem(
    val tab: SpectraTab,
    val icon: ImageVector,
    val label: String,
    val count: Int? = null,
)
