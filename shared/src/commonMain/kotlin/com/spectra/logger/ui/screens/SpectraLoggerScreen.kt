package com.spectra.logger.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.spectra.logger.SpectraLogger

/**
 * Main Spectra Logger screen with tabs for logs and network logs.
 *
 * @param modifier Modifier for the screen
 */
@Composable
fun SpectraLoggerScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Logs") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Network") },
            )
        }

        when (selectedTab) {
            0 -> LogListScreen(storage = SpectraLogger.logStorage)
            1 -> NetworkLogScreen(storage = SpectraLogger.networkStorage)
        }
    }
}
