package com.spectra.logger.feature.network.ui

import com.spectra.logger.core.ui.components.charts.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.ui.components.effects.*
import com.spectra.logger.core.ui.components.common.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.core.ui.components.charts.TimelineBarChart
import com.spectra.logger.core.ui.model.BarChartData
import com.spectra.logger.core.ui.theme.SpectraDesignTokens
import com.spectra.logger.feature.network.statistics.NetworkDashboardStatistics
import com.spectra.logger.feature.network.ui.components.StatusPieChart
import com.spectra.logger.feature.network.ui.components.colorForStatus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun NetworkDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkStatisticsViewModel = viewModel { NetworkStatisticsViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()

    NetworkDashboardContent(
        statistics = uiState.statistics,
        modifier = modifier,
    )
}

@Composable
fun NetworkDashboardContent(
    statistics: NetworkDashboardStatistics,
    modifier: Modifier = Modifier,
) {
    // 1. Request Volume Timeline data
    val volumeChartData = remember(statistics.timeline) {
        statistics.timeline.map { bucket ->
            val instant = Instant.fromEpochMilliseconds(bucket.timestamp)
            val time = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val timeString = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

            BarChartData(
                id = bucket.timestamp,
                value = bucket.count.toFloat(),
                color = SpectraDesignTokens.DebugBlue, // Indigo/Blue for volume
                label = timeString,
            )
        }.toImmutableList()
    }

    val maxVolume = remember(volumeChartData) {
        volumeChartData.maxOfOrNull { it.value } ?: 0f
    }

    // 2. Latency Timeline data
    val latencyChartData = remember(statistics.timeline) {
        statistics.timeline.map { bucket ->
            val instant = Instant.fromEpochMilliseconds(bucket.timestamp)
            val time = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val timeString = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

            BarChartData(
                id = bucket.timestamp,
                value = bucket.averageDurationMs.toFloat(),
                color = SpectraDesignTokens.InfoGreen, // Emerald for latency
                label = timeString,
            )
        }.toImmutableList()
    }

    val maxLatency = remember(latencyChartData) {
        latencyChartData.maxOfOrNull { it.value } ?: 0f
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 350.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        // Request Volume Timeline
        item(span = { GridItemSpan(maxLineSpan) }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Request Volume Over Time", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (volumeChartData.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No request activity recorded", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        TimelineBarChart(
                            data = volumeChartData,
                            maxValue = maxVolume,
                            onRangeSelected = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2.5f)
                                .heightIn(min = 180.dp, max = 350.dp),
                        )
                    }
                }
            }
        }

        // Latency Timeline
        item(span = { GridItemSpan(maxLineSpan) }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Average Latency Over Time (ms)", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (latencyChartData.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No latency data recorded", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        TimelineBarChart(
                            data = latencyChartData,
                            maxValue = maxLatency,
                            onRangeSelected = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2.5f)
                                .heightIn(min = 180.dp, max = 350.dp),
                        )
                    }
                }
            }
        }

        // Status Code Distribution (Donut slice layout)
        item(span = { GridItemSpan(1) }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Status Code Distribution", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusPieChart(
                        statusCounts = statistics.statusCounts,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                            .heightIn(min = 150.dp, max = 300.dp),
                        isEmpty = statistics.totalRequests == 0,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusLegend(
                        statusCounts = statistics.statusCounts,
                        total = statistics.totalRequests,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Response Time Breakdown (Linear indicators)
        item(span = { GridItemSpan(1) }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Latency Breakdown", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val total = statistics.totalRequests.coerceAtLeast(1)
                        val buckets = listOf(
                            "< 100ms" to SpectraDesignTokens.InfoGreen,
                            "100-500ms" to SpectraDesignTokens.DebugBlue,
                            "500ms-2s" to SpectraDesignTokens.WarningOrange,
                            "> 2s" to SpectraDesignTokens.ErrorRed
                        )

                        buckets.forEach { (bucket, color) ->
                            val count = statistics.latencyDistribution[bucket] ?: 0
                            val pct = (count.toFloat() / total * 100).toInt()
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = bucket, style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = "$count request${if (count != 1) "s" else ""} ($pct%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / total },
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusLegend(
    statusCounts: Map<String, Int>,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val safeTotal = total.coerceAtLeast(1)
    val statusKeys = listOf("2xx", "3xx", "4xx", "5xx", "Failed")

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        statusKeys.forEach { key ->
            val count = statusCounts[key] ?: 0
            val percentage = ((count.toFloat() / safeTotal) * 100).toInt()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colorForStatus(key)),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$key: $count ($percentage%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
