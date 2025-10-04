package com.spectra.logger.example

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
import kotlinx.coroutines.launch

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
                        SpectraLogger.i("Export", "Export logs requested")
                    }
                },
            )
        }
    }
}
