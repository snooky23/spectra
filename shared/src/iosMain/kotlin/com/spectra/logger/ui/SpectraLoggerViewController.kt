package com.spectra.logger.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.spectra.logger.SpectraLogger
import com.spectra.logger.ui.screens.LogListScreen
import com.spectra.logger.ui.screens.NetworkLogScreen
import com.spectra.logger.ui.screens.SettingsScreen
import platform.UIKit.UIViewController

/**
 * Creates a UIViewController containing the complete Spectra Logger screen.
 * This is the main SDK component for iOS - one screen with all tabs included.
 *
 * Usage from Swift:
 * ```swift
 * import SpectraLogger
 *
 * // Present the complete logger UI
 * let loggerVC = SpectraLoggerViewControllerKt.createSpectraLoggerViewController()
 * present(loggerVC, animated: true)
 * ```
 *
 * What's included automatically:
 * - Logs tab with filtering and search
 * - Network logs tab
 * - Settings tab with export functionality
 * - Tab navigation with live counts
 */
fun createSpectraLoggerViewController(): UIViewController {
    return ComposeUIViewController {
        MaterialTheme {
            var selectedTab by remember { mutableIntStateOf(0) }

            Scaffold { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Logs") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Network") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Settings") }
                        )
                    }

                    when (selectedTab) {
                        0 -> LogListScreen(storage = SpectraLogger.logStorage)
                        1 -> NetworkLogScreen(storage = SpectraLogger.networkStorage)
                        2 -> SettingsScreen(
                            logStorage = SpectraLogger.logStorage,
                            networkLogStorage = SpectraLogger.networkStorage
                        )
                    }
                }
            }
        }
    }
}
