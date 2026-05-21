package com.spectra.logger.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.statistics.DashboardStatistics
import com.spectra.logger.ui.compose.components.LevelPieChart
import com.spectra.logger.ui.compose.components.LogLevelLegend
import com.spectra.logger.ui.compose.components.TimelineBarChart
import com.spectra.logger.ui.compose.components.colorForLogLevel
import com.spectra.logger.ui.compose.model.BarChartData
import kotlinx.collections.immutable.toImmutableList

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    statisticsViewModel: StatisticsViewModel = viewModel { StatisticsViewModel() },
    onTimeRangeSelected: (Long, Long) -> Unit = { _, _ -> },
    onLevelTapped: (LogLevel) -> Unit = {},
) {
    val uiState by statisticsViewModel.uiState.collectAsState()

    DashboardContent(
        statistics = uiState.statistics,
        onTimeRangeSelected = onTimeRangeSelected,
        onLevelTapped = onLevelTapped,
        modifier = modifier
    )
}

@Composable
fun DashboardContent(
    statistics: DashboardStatistics,
    onTimeRangeSelected: (Long, Long) -> Unit,
    onLevelTapped: (LogLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val barChartData = remember(statistics.timeline) {
        statistics.timeline.map { bucket ->
            val totalCount = bucket.counts.values.sum()
            // Find most severe level
            val mostSevereLevel = bucket.counts.keys.maxByOrNull { it.ordinal } ?: LogLevel.INFO
            val color = colorForLogLevel(mostSevereLevel)

            BarChartData(
                id = bucket.timestamp,
                value = totalCount.toFloat(),
                color = color,
                label = bucket.timestamp.toString()
            )
        }.toImmutableList()
    }

    val maxValue = remember(barChartData) {
        barChartData.maxOfOrNull { it.value } ?: 0f
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Log Activity Over Time", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TimelineBarChart(
                        data = barChartData,
                        maxValue = maxValue,
                        onRangeSelected = { intRange ->
                            if (intRange.first in barChartData.indices && intRange.last in barChartData.indices) {
                                val startTimestamp = barChartData[intRange.first].id
                                val endTimestamp = barChartData[intRange.last].id
                                onTimeRangeSelected(startTimestamp, endTimestamp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().aspectRatio(2f).heightIn(min = 200.dp, max = 400.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LogLevelLegend(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item(span = { GridItemSpan(1) }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Severity Distribution", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LevelPieChart(
                        levelCounts = statistics.levelCounts,
                        onLevelClick = onLevelTapped,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.5f).heightIn(min = 150.dp, max = 300.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LogLevelLegend(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item(span = { GridItemSpan(1) }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Label, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Top Subsystems", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth().height(250.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val topTags = statistics.tagCounts.entries.sortedByDescending { it.value }.take(5)
                        val maxCount = topTags.maxOfOrNull { it.value }?.toFloat() ?: 1f
                        if (topTags.isEmpty()) {
                            Text(text = "No tag data available", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            topTags.forEach { (tag, count) ->
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = tag, style = MaterialTheme.typography.labelMedium)
                                        Text(text = count.toString(), style = MaterialTheme.typography.labelMedium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { count / maxCount },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
