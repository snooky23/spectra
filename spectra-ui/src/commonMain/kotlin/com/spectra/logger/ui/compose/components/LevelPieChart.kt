package com.spectra.logger.ui.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.spectra.logger.domain.model.LogLevel
import kotlin.math.PI
import kotlin.math.atan2

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
    selectedLevel: LogLevel? = null,
    onLevelClick: ((LogLevel) -> Unit)? = null,
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
    val hapticFeedback = LocalHapticFeedback.current

    var tapOffset by remember { mutableStateOf<Offset?>(null) }
    if (selectedLevel == null) {
        tapOffset = null
    }

    BoxWithConstraints(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

        val canvasModifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(levelCounts, onLevelClick) {
                if (onLevelClick == null) return@pointerInput
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y

                    // Calculate angle in degrees from -180 to 180
                    var angle = (atan2(dy.toDouble(), dx.toDouble()) * (180 / PI)).toFloat()
                    // Shift so 0 is at 12 o'clock (-90 degrees in atan2 standard)
                    angle += 90f
                    if (angle < 0f) angle += 360f

                    var currentAngle = 0f
                    for ((level, count) in levelCounts) {
                        if (count > 0) {
                            val sweep = (count.toFloat() / total) * 360f
                            if (angle >= currentAngle && angle < currentAngle + sweep) {
                                tapOffset = offset
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLevelClick(level)
                                break
                            }
                            currentAngle += sweep
                        }
                    }
                }
            }

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
        val gapAngle = if (activeSlicesCount > 1) 3f else 0f

        // To ensure stable slice rendering, order by ordinal or count if needed,
        // but default map iteration is fine if predictable.
        levelCounts.forEach { (level, count) ->
            if (count > 0) {
                val sweepAngle = (count.toFloat() / total) * 360f
                val adjustedSweep = sweepAngle - gapAngle
                val color = colorForLogLevel(level)

                val isSelected = selectedLevel == level
                val hasSelection = selectedLevel != null
                val alpha = if (hasSelection && !isSelected) 0.4f else 1.0f

                drawArc(
                    color = color.copy(alpha = alpha),
                    startAngle = startAngle + (gapAngle / 2f),
                    sweepAngle = adjustedSweep,
                    useCenter = useCenter && strokeWidth == null,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style,
                )

                startAngle += sweepAngle
            }
        }
    }

        if (selectedLevel != null && tapOffset != null) {
            val count = levelCounts[selectedLevel] ?: 0
            val isRightHalf = tapOffset!!.x > maxWidthPx / 2f
            val xOffset = if (isRightHalf) {
                with(LocalDensity.current) { (tapOffset!!.x - 120.dp.toPx()).toDp() }
            } else {
                with(LocalDensity.current) { (tapOffset!!.x + 20.dp.toPx()).toDp() }
            }
            val yOffset = with(LocalDensity.current) { (tapOffset!!.y - 30.dp.toPx()).toDp() }

            GraphTooltip(
                text = "${selectedLevel.name}: $count",
                modifier = Modifier.offset(x = xOffset, y = yOffset)
            )
        }
    }
}
