package com.spectra.logger.ui.compose.model

import androidx.compose.ui.graphics.Color

/**
 * Immutable UI model representing a single bar in the TimelineBarChart.
 * This completely decouples the Canvas drawing logic from the core domain structures.
 */
data class BarChartData(
    val id: Long,
    val value: Float,
    val color: Color,
    val label: String,
)
