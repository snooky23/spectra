package com.spectra.logger.core.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spectra.logger.core.model.*
import com.spectra.logger.core.ui.components.common.*
import com.spectra.logger.core.ui.components.effects.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.ui.model.BarChartData
import com.spectra.logger.core.utils.*
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun TimelineBarChartPreview() {
    MaterialTheme {
        Box(modifier = Modifier.width(400.dp).height(200.dp).background(MaterialTheme.colorScheme.surface)) {
            val sampleData =
                persistentListOf(
                    BarChartData(1L, 10f, Color.Red, "10:00"),
                    BarChartData(2L, 50f, Color.Yellow, "10:01"),
                    BarChartData(3L, 20f, Color.Green, "10:02"),
                    // Peak
                    BarChartData(4L, 100f, Color.Red, "10:03"),
                    BarChartData(5L, 0f, Color.Green, "10:04"),
                    BarChartData(6L, 30f, Color.Yellow, "10:05"),
                )

            TimelineBarChart(
                data = sampleData,
                maxValue = 100f,
            )
        }
    }
}

@Preview
@Composable
fun TimelineBarChartLoadingPreview() {
    MaterialTheme {
        Box(modifier = Modifier.width(400.dp).height(200.dp).background(MaterialTheme.colorScheme.surface)) {
            TimelineBarChart(
                data = persistentListOf(),
                maxValue = 100f,
                isLoading = true,
            )
        }
    }
}

@Preview
@Composable
fun TimelineBarChartEmptyPreview() {
    MaterialTheme {
        Box(modifier = Modifier.width(400.dp).height(200.dp).background(MaterialTheme.colorScheme.surface)) {
            TimelineBarChart(
                data = persistentListOf(),
                maxValue = 100f,
                isEmpty = true,
            )
        }
    }
}
