package com.spectra.logger.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.LogLevel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun LevelPieChartPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.surface)) {
            val sampleData =
                mapOf(
                    LogLevel.INFO to 150,
                    LogLevel.ERROR to 20,
                    LogLevel.WARNING to 45,
                    LogLevel.DEBUG to 200,
                )

            LevelPieChart(
                levelCounts = sampleData,
            )
        }
    }
}

@Preview
@Composable
fun LevelPieChartLoadingPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.surface)) {
            LevelPieChart(
                levelCounts = emptyMap(),
                isLoading = true,
            )
        }
    }
}

@Preview
@Composable
fun LevelPieChartEmptyPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.surface)) {
            LevelPieChart(
                levelCounts = emptyMap(),
                isEmpty = true,
            )
        }
    }
}
