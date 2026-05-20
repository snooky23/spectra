package com.spectra.logger.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.statistics.DashboardStatistics
import com.spectra.logger.ui.compose.components.LevelPieChart
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
                modifier = Modifier.fillMaxSize()
            )
        }
        item(span = { GridItemSpan(1) }) {
            LevelPieChart(
                levelCounts = statistics.levelCounts,
                onLevelClick = onLevelTapped,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
