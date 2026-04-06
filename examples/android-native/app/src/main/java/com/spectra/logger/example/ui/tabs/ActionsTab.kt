package com.spectra.logger.example.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spectra.logger.example.BuildConfig
import com.spectra.logger.example.ui.components.BrandingCard
import com.spectra.logger.example.ui.components.LogButton
import com.spectra.logger.example.ui.components.OpenSpectraButton
import com.spectra.logger.example.ui.components.SectionHeader
import com.spectra.logger.example.viewmodel.ActionsViewModel

@Composable
fun ActionsTab(
    viewModel: ActionsViewModel,
    onOpenSpectra: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            BrandingCard(
                icon = Icons.Default.CheckCircle,
                title = "Example App",
                subtitle = "with Spectra Logger Integration"
            )
        }

        item { SectionHeader("Example Actions") }

        item {
            LogButton(
                label = "Tap Me (Generates Log)",
                icon = Icons.Default.ThumbUp,
                backgroundColor = Color(0xFF2196F3),
                action = viewModel::logButtonTapped
            )
        }

        item {
            LogButton(
                label = "Generate Warning",
                icon = Icons.Default.Warning,
                backgroundColor = Color(0xFFFF9800),
                action = viewModel::generateWarning
            )
        }

        item {
            LogButton(
                label = "Generate Error",
                icon = Icons.Default.Error,
                backgroundColor = Color(0xFFF44336),
                action = viewModel::generateError
            )
        }

        item {
            LogButton(
                label = "Error with Stack Trace",
                icon = Icons.Default.Info,
                backgroundColor = Color(0xFFF44336),
                action = viewModel::generateErrorWithStackTrace
            )
        }

        item { SectionHeader("Batch Logging") }

        item {
            LogButton(
                label = "Generate 10 Logs",
                icon = Icons.Default.List,
                backgroundColor = Color(0xFF9C27B0),
                action = viewModel::generate10Logs
            )
        }

        item {
            LogButton(
                label = "Generate 100 Logs",
                icon = Icons.Default.Storage,
                backgroundColor = Color(0xFF9C27B0),
                action = viewModel::generate100Logs
            )
        }

        item { SectionHeader("Real-Time Demo") }

        item {
            LogButton(
                label = if (viewModel.isBackgroundLogging) "⏹ Stop Background Logging" else "▶ Start Background Logging",
                icon = if (viewModel.isBackgroundLogging) Icons.Default.Stop else Icons.Default.PlayArrow,
                backgroundColor = if (viewModel.isBackgroundLogging) Color.Red else Color.Green,
                action = viewModel::toggleBackgroundLogging
            )
        }

        item {
            Text(
                text = "Logs every 2 seconds while running",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item { SectionHeader("Filtering Demos") }

        item {
            LogButton(
                label = "Generate All Log Levels",
                icon = Icons.Default.Tune,
                backgroundColor = Color(0xFF607D8B),
                action = viewModel::generateAllLogLevels
            )
        }

        item {
            LogButton(
                label = "Generate Searchable Logs",
                icon = Icons.Default.Search,
                backgroundColor = Color(0xFF607D8B),
                action = viewModel::generateSearchableLogs
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (BuildConfig.DEBUG) {
            item {
                OpenSpectraButton(onClick = onOpenSpectra)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
