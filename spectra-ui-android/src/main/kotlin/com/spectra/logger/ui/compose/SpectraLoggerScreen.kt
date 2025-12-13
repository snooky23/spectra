package com.spectra.logger.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.spectra.logger.R

/**
 * Main Spectra Logger Compose UI with bottom navigation.
 * Matches the iOS SwiftUI design with 3 tabs: Logs, Network, Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpectraLoggerScreen(onDismiss: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs =
        listOf(
            TabItem("Logs", "list.bullet.rectangle"),
            TabItem("Network", "network"),
            TabItem("Settings", "gearshape"),
        )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = getIconResource(tab.icon)),
                                contentDescription = tab.title,
                            )
                        },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                    )
                }
            }
        },
    ) { paddingValues ->
        when (selectedTab) {
            0 -> LogsScreen(modifier = Modifier.padding(paddingValues))
            1 -> NetworkLogsScreen(modifier = Modifier.padding(paddingValues))
            2 ->
                SettingsScreen(
                    modifier = Modifier.padding(paddingValues),
                    onDismiss = onDismiss,
                )
        }
    }
}

private data class TabItem(
    val title: String,
    val icon: String,
)

private fun getIconResource(iconName: String): Int {
    return when (iconName) {
        "list.bullet.rectangle" -> android.R.drawable.ic_menu_sort_by_size
        "network" -> android.R.drawable.ic_menu_share
        "gearshape" -> android.R.drawable.ic_menu_preferences
        else -> android.R.drawable.ic_menu_info_details
    }
}
