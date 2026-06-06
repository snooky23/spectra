package com.spectra.logger.core.ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.core.model.*
import com.spectra.logger.core.ui.components.charts.*
import com.spectra.logger.core.ui.components.common.*
import com.spectra.logger.core.ui.components.common.SpectraTheme
import com.spectra.logger.core.ui.components.effects.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.feature.logs.ui.LogsScreen
import com.spectra.logger.feature.network.ui.NetworkLogsScreen
import com.spectra.logger.feature.settings.ui.SettingsScreen
import com.spectra.logger.feature.settings.ui.SettingsViewModel

/**
 * Main Spectra Logger UI entry point using adaptive navigation.
 * Automatically switches between bottom bar and side rail based on screen width.
 */
@Composable
fun SpectraLoggerScreen(
    onDismiss: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel() },
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val settingsState by settingsViewModel.uiState.collectAsState()

    val tabs =
        listOf(
            TabItem("Logs", Icons.AutoMirrored.Filled.ListAlt),
            TabItem("Network", Icons.Default.SettingsEthernet),
            TabItem("Settings", Icons.Default.Settings),
        )

    SpectraTheme(appearanceMode = settingsState.appearanceMode) {
        NavigationSuiteScaffold(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            navigationSuiteItems = {
                tabs.forEachIndexed { index, tab ->
                    item(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                            )
                        },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                    )
                }
            },
        ) {
            when (selectedTab) {
                0 -> LogsScreen(onDismiss = onDismiss)
                1 -> NetworkLogsScreen(onDismiss = onDismiss)
                2 ->
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onDismiss = onDismiss,
                    )
            }
        }
    }
}

private data class TabItem(
    val title: String,
    val icon: ImageVector,
)
