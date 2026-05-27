package com.spectra.logger.core.ui.model

import com.spectra.logger.core.ui.components.charts.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.ui.components.effects.*
import com.spectra.logger.core.ui.components.common.*

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

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
