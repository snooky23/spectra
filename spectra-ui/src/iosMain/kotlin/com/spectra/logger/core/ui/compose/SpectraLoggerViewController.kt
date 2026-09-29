package com.spectra.logger.core.ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.spectra.logger.core.ui.components.common.SpectraTheme
import com.spectra.logger.feature.crash.ui.CrashHistoryDialog
import com.spectra.logger.feature.crash.ui.CrashViewModel
import com.spectra.logger.feature.events.ui.EventsScreen
import com.spectra.logger.feature.logs.ui.LogsScreen
import com.spectra.logger.feature.network.ui.NetworkLogsScreen
import com.spectra.logger.feature.settings.ui.SettingsScreen
import com.spectra.logger.feature.settings.ui.SettingsViewModel
import com.spectra.logger.feature.streaming.ui.RemoteStreamDialog
import com.spectra.logger.feature.streaming.ui.RemoteStreamViewModel
import platform.UIKit.UIViewController

/**
 * Creates a UIViewController that hosts the Spectra Logger UI with Compose navigation.
 * This is used by iOS applications to present the logger.
 */
fun SpectraLoggerViewController(onDismiss: () -> Unit = {}): UIViewController =
    ComposeUIViewController(configure = {
        enforceStrictPlistSanityCheck = false
    }) {
        SpectraLoggerScreen(onDismiss = onDismiss)
    }

/**
 * Creates a UIViewController for an individual Spectra tab (Logs, Network, Events, Settings).
 * This enables native iOS navigation architectures (e.g. SwiftUI TabView with iOS 26 Liquid Glass)
 * without nested Compose bottom bars.
 */
fun SpectraTabViewController(
    tabIndex: Int,
    onDismiss: () -> Unit = {},
): UIViewController =
    ComposeUIViewController(configure = {
        enforceStrictPlistSanityCheck = false
    }) {
        SpectraTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                when (tabIndex) {
                    0 -> LogsScreen(onDismiss = onDismiss)
                    1 -> NetworkLogsScreen(onDismiss = onDismiss)
                    2 -> EventsScreen(onDismiss = onDismiss)
                    3 -> {
                        val settingsViewModel = remember { SettingsViewModel() }
                        val streamViewModel = remember { RemoteStreamViewModel() }
                        val crashViewModel = remember { CrashViewModel() }
                        var showRemoteStreamDialog by remember { mutableStateOf(false) }
                        var showCrashDialog by remember { mutableStateOf(false) }

                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onDismiss = onDismiss,
                            onOpenRemoteStreamDialog = { showRemoteStreamDialog = true },
                            onOpenCrashHistoryDialog = { showCrashDialog = true },
                        )

                        if (showRemoteStreamDialog) {
                            RemoteStreamDialog(
                                viewModel = streamViewModel,
                                onDismiss = { showRemoteStreamDialog = false },
                            )
                        }

                        if (showCrashDialog) {
                            CrashHistoryDialog(
                                viewModel = crashViewModel,
                                onDismiss = { showCrashDialog = false },
                            )
                        }
                    }
                    else -> LogsScreen(onDismiss = onDismiss)
                }
            }
        }
    }
