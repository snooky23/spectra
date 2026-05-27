package com.spectra.logger.feature.network.ui.components

import com.spectra.logger.core.ui.components.charts.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.ui.components.effects.*
import com.spectra.logger.core.ui.components.common.*
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.spectra.logger.core.ui.theme.SpectraDesignTokens
import kotlin.math.PI

/**
 * A stateless Compose Canvas component used to render the proportion of Network Response Status Code Families.
 */
@Composable
fun StatusPieChart(
    statusCounts: Map<String, Int>,
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
                    isLoading -> "Status code distribution chart: Loading data"
                    isEmpty -> "Status code distribution chart: No data available"
                    else -> {
                        val parts = statusCounts.entries
                            .filter { it.value > 0 }
                            .map { (status, cnt) -> "$status: $cnt" }
                        val distribution = parts.joinToString(", ")
                        "Status code distribution. $distribution"
                    }
                }
        }
    val baseModifier = modifier.then(semanticsModifier)

    if (isLoading) {
        Box(
            modifier = baseModifier
                .size(200.dp)
                .clip(CircleShape)
                .shimmerEffect(),
        )
        return
    }

    if (isEmpty) {
        EmptyState(
            icon = Icons.Default.Info,
            message = "No status code data available",
            modifier = baseModifier,
        )
        return
    }

    val total = remember(statusCounts) { statusCounts.values.sum().coerceAtLeast(1) }
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = baseModifier,
        contentAlignment = Alignment.Center,
    ) {
        val canvasModifier = Modifier
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

            val style = if (strokeWidth != null) {
                Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            } else {
                Fill
            }

            val activeSlicesCount = statusCounts.values.count { it > 0 }
            val gapAngle = if (activeSlicesCount > 1) {
                if (strokeWidth != null) 3f else 1.5f
            } else {
                0f
            }

            statusCounts.forEach { (status, count) ->
                if (count > 0) {
                    val sweepAngle = (count.toFloat() / total) * 360f
                    val adjustedSweep = sweepAngle - gapAngle
                    val color = colorForStatus(status)

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
                        val textLayoutResult = textMeasurer.measure(
                            text = "$percentage%",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )

                        val angleInRadians = (startAngle + gapAngle / 2f + adjustedSweep / 2f) * (PI / 180f)
                        val radius = arcSize.width / 2f
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

fun colorForStatus(status: String): Color {
    return when (status) {
        "2xx" -> SpectraDesignTokens.InfoGreen
        "3xx" -> SpectraDesignTokens.DebugBlue
        "4xx" -> SpectraDesignTokens.WarningOrange
        "5xx" -> SpectraDesignTokens.ErrorRed
        else -> SpectraDesignTokens.FatalPurple
    }
}
