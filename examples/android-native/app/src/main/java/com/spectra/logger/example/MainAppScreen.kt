package com.spectra.logger.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.example.ui.tabs.ActionsTab
import com.spectra.logger.example.ui.tabs.NetworkTab
import com.spectra.logger.example.viewmodel.ActionsViewModel
import com.spectra.logger.example.viewmodel.NetworkViewModel

@Composable
fun MainAppScreen(
    onOpenSpectra: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val actionsViewModel: ActionsViewModel = viewModel()
    val networkViewModel: NetworkViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("Actions") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Language, contentDescription = null) },
                    label = { Text("Network") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ActionsTab(
                    viewModel = actionsViewModel,
                    onOpenSpectra = onOpenSpectra
                )
                1 -> NetworkTab(
                    viewModel = networkViewModel,
                    onOpenSpectra = onOpenSpectra
                )
            }
        }
    }
}
