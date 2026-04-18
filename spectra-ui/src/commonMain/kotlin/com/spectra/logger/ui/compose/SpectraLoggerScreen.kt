package com.spectra.logger.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.ui.compose.components.SpectraTheme

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
            TabItem("Logs", Icons.Default.ListAlt),
            TabItem("Network", Icons.Default.SettingsEthernet),
            TabItem("Settings", Icons.Default.Settings),
        )

    SpectraTheme(appearanceMode = settingsState.appearanceMode) {
        NavigationSuiteScaffold(
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
