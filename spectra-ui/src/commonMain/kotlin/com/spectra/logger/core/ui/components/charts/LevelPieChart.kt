package com.spectra.logger.core.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spectra.logger.core.model.*
import com.spectra.logger.core.ui.components.common.*
import com.spectra.logger.core.ui.components.effects.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.feature.logs.model.LogLevel
import kotlin.math.PI

/**
 * A stateless Compose Canvas primitive used to render the distribution of log levels proportionally.
 *
 * @param levelCounts Map of LogLevel to count to display
 * @param modifier Modifier for styling/layout
 * @param useCenter Whether to draw pie slices to the center (true for solid pie chart)
 * @param strokeWidth If provided, draws a donut chart with the specified stroke width instead of a solid pie
 * @param isLoading Whether the component should show a shimmer loading skeleton.
 * @param isEmpty Whether the component should show an empty state.
 * @param selectedLevel The currently selected LogLevel, if any.
 * @param onLevelClick Callback when a pie slice is tapped.
 */
@Composable
fun LevelPieChart(
    levelCounts: Map<LogLevel, Int>,
    modifier: Modifier = Modifier,
    useCenter: Boolean = true,
    strokeWidth: Float? = null,
    isLoading: Boolean = false,
    isEmpty: Boolean = false,
) {
    val semanticsModifier =
        Modifier.clearAndSetSemantics {
            contentDescription =
                when {
                    isLoading -> "Log level distribution chart: Loading data"
                    isEmpty -> "Log level distribution chart: No data available"
                    else -> {
                        val parts =
                            levelCounts.entries
                                .filter { it.value > 0 }
                                .map { (lvl, cnt) ->
                                    val name = lvl.name.lowercase().replaceFirstChar { it.uppercase() }
                                    "$name: $cnt"
                                }
                        val distribution = parts.joinToString(", ")
                        "Log level distribution. $distribution"
                    }
                }
        }
    val baseModifier = modifier.then(semanticsModifier)
    if (isLoading) {
        Box(
            modifier =
                baseModifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .shimmerEffect(),
        )
        return
    }

    if (isEmpty) {
        EmptyState(
            icon = Icons.Default.Info,
            message = "No distribution data available",
            modifier = baseModifier,
        )
        return
    }
    val total = remember(levelCounts) { levelCounts.values.sum().coerceAtLeast(1) }
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    BoxWithConstraints(
        modifier = baseModifier,
        contentAlignment = Alignment.Center,
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

        val canvasModifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)

        Canvas(modifier = canvasModifier) {
            var startAngle = -90f // Start drawing from the top (12 o'clock)

            // Force perfect circle by using the minimum dimension
            val minDim = kotlin.math.min(size.width, size.height)
            val rawTopLeft = Offset((size.width - minDim) / 2f, (size.height - minDim) / 2f)

            // Deflate bounding box to prevent stroke clipping
            val offsetPadding = (strokeWidth ?: 0f) / 2f
            val topLeft = Offset(rawTopLeft.x + offsetPadding, rawTopLeft.y + offsetPadding)
            val arcSize = Size(minDim - (strokeWidth ?: 0f), minDim - (strokeWidth ?: 0f))

            val style =
                if (strokeWidth != null) {
                    Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                } else {
                    Fill
                }

            val activeSlicesCount = levelCounts.values.count { it > 0 }
            val gapAngle =
                if (activeSlicesCount > 1) {
                    if (strokeWidth != null) 3f else 1.5f
                } else {
                    0f
                }

            // To ensure stable slice rendering, order by ordinal or count if needed,
            // but default map iteration is fine if predictable.
            levelCounts.forEach { (level, count) ->
                if (count > 0) {
                    val sweepAngle = (count.toFloat() / total) * 360f
                    val adjustedSweep = sweepAngle - gapAngle
                    val color = colorForLogLevel(level)

                    drawArc(
                        color = color,
                        startAngle = startAngle + (gapAngle / 2f),
                        sweepAngle = adjustedSweep,
                        useCenter = useCenter && strokeWidth == null,
                        topLeft = topLeft,
                        size = arcSize,
                        style = style,
                    )

                    if (adjustedSweep >= 18f) { // Only draw text if slice is > 5%
                        val percentage = ((count.toFloat() / total) * 100).toInt()
                        val textLayoutResult =
                            textMeasurer.measure(
                                text = "$percentage%",
                                style =
                                    androidx.compose.ui.text.TextStyle(
                                        color = androidx.compose.ui.graphics.Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    ),
                            )

                        val angleInRadians = (startAngle + gapAngle / 2f + adjustedSweep / 2f) * (PI / 180f)
                        val radius = arcSize.width / 2f
                        // If stroke is used (donut), place text in middle of stroke, else 65% out from center
                        val r = if (strokeWidth != null) radius else radius * 0.65f
                        val cx = topLeft.x + radius
                        val cy = topLeft.y + radius

                        val textX = cx + r * kotlin.math.cos(angleInRadians).toFloat() - textLayoutResult.size.width / 2f
                        val textY = cy + r * kotlin.math.sin(angleInRadians).toFloat() - textLayoutResult.size.height / 2f

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(textX, textY),
                        )
                    }

                    startAngle += sweepAngle
                }
            }
        }
    }
}
