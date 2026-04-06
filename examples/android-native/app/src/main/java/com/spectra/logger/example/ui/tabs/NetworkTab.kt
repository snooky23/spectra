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
import com.spectra.logger.example.viewmodel.NetworkViewModel

@Composable
fun NetworkTab(
    viewModel: NetworkViewModel,
    onOpenSpectra: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            BrandingCard(
                icon = Icons.Default.Language,
                title = "Network Testing",
                subtitle = "Simulate HTTP requests and responses"
            )
        }

        item { SectionHeader("Network Requests") }

        item {
            LogButton(
                label = "GET Request (200 OK)",
                icon = Icons.Default.ArrowCircleDown,
                backgroundColor = Color.Green,
                action = {
                    viewModel.simulateRequest(
                        method = "GET",
                        url = "https://api.example.com/users",
                        statusCode = 200,
                        duration = 0.5
                    )
                }
            )
        }

        item {
            LogButton(
                label = "POST Request (201 Created)",
                icon = Icons.Default.AddCircle,
                backgroundColor = Color.Green,
                action = {
                    viewModel.simulateRequest(
                        method = "POST",
                        url = "https://api.example.com/users",
                        statusCode = 201,
                        duration = 1.0
                    )
                }
            )
        }

        item {
            LogButton(
                label = "GET Request (404 Not Found)",
                icon = Icons.Default.HelpCenter,
                backgroundColor = Color.Yellow,
                action = {
                    viewModel.simulateRequest(
                        method = "GET",
                        url = "https://api.example.com/users/9999",
                        statusCode = 404,
                        duration = 0.3
                    )
                }
            )
        }

        item {
            LogButton(
                label = "Server Error (500)",
                icon = Icons.Default.ErrorOutline,
                backgroundColor = Color.Red,
                action = {
                    viewModel.simulateRequest(
                        method = "GET",
                        url = "https://api.example.com/data",
                        statusCode = 500,
                        duration = 2.0
                    )
                }
            )
        }

        item { SectionHeader("More HTTP Methods") }

        item {
            LogButton(
                label = "PUT Request (200 OK)",
                icon = Icons.Default.ArrowCircleUp,
                backgroundColor = Color.Green,
                action = {
                    viewModel.simulateRequest(
                        method = "PUT",
                        url = "https://api.example.com/users/123",
                        statusCode = 200,
                        duration = 0.6
                    )
                }
            )
        }

        item {
            LogButton(
                label = "DELETE Request (204 No Content)",
                icon = Icons.Default.Delete,
                backgroundColor = Color.Green,
                action = {
                    viewModel.simulateRequest(
                        method = "DELETE",
                        url = "https://api.example.com/users/456",
                        statusCode = 204,
                        duration = 0.4
                    )
                }
            )
        }

        item { SectionHeader("Batch Network") }

        item {
            LogButton(
                label = "Simulate 10 API Calls",
                icon = Icons.Default.Dns,
                backgroundColor = Color.Cyan,
                action = viewModel::simulate10ApiCalls
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
