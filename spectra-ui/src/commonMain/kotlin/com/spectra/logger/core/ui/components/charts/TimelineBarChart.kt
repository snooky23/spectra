package com.spectra.logger.core.ui.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spectra.logger.core.model.*
import com.spectra.logger.core.ui.components.common.*
import com.spectra.logger.core.ui.components.effects.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.ui.model.BarChartData
import com.spectra.logger.core.utils.*
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.max
import kotlin.math.min

/**
 * A stateless, pure Canvas component for rendering the Timeline Bar Chart.
 * The graph dynamically compresses all items to fit the available screen width.
 *
 * @param data Immutable list of data points to plot.
 * @param maxValue The absolute maximum value across the dataset, used to scale 100% height.
 * @param modifier Standard Compose modifier.
 * @param isLoading Whether the component should show a shimmer loading skeleton.
 * @param isEmpty Whether the component should show an empty state.
 * @param selectedIndex The currently selected bar index, if any.
 * @param onElementClick Callback when a bar is tapped.
 */
@Composable
fun TimelineBarChart(
    data: ImmutableList<BarChartData>,
    maxValue: Float,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isEmpty: Boolean = false,
    selectedIndex: Int? = null,
    onElementClick: ((Int) -> Unit)? = null,
    onRangeSelected: ((IntRange) -> Unit)? = null,
) {
    val semanticsModifier =
        Modifier.clearAndSetSemantics {
            contentDescription =
                when {
                    isLoading -> "Timeline bar chart: Loading data"
                    isEmpty -> "Timeline bar chart: No data available"
                    else -> "Timeline bar chart showing ${data.size} items with a peak of ${maxValue.toInt()} logs."
                }
        }
    val baseModifier = modifier.then(semanticsModifier)
    if (isLoading) {
        Box(
            modifier = baseModifier.fillMaxSize().shimmerEffect(),
        )
        return
    }

    if (isEmpty) {
        EmptyState(
            icon = Icons.Default.Info,
            message = "No log data available in this timeframe",
            modifier = baseModifier,
        )
        return
    }
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val selectionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val hapticFeedback = LocalHapticFeedback.current

    val alphas =
        data.indices.map { index ->
            val isSelected = selectedIndex == index
            val hasSelection = selectedIndex != null
            animateFloatAsState(
                targetValue = if (hasSelection && !isSelected) 0.4f else 1.0f,
                label = "alpha_$index",
            ).value
        }

    val scales =
        data.indices.map { index ->
            val isSelected = selectedIndex == index
            animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                label = "scale_$index",
            ).value
        }

    var tapOffset by remember { mutableStateOf<Offset?>(null) }
    if (selectedIndex == null) {
        tapOffset = null
    }

    var dragStartOffset by remember { mutableStateOf<Float?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = baseModifier.fillMaxSize()) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val density = LocalDensity.current
        val leftPadding =
            if (data.isNotEmpty()) {
                textMeasurer.measure(
                    text = maxValue.toInt().toString(),
                    style = TextStyle(fontSize = 10.sp),
                ).size.width.toFloat() + with(density) { 8.dp.toPx() }
            } else {
                0f
            }
        val bottomPadding = if (data.isNotEmpty()) with(density) { 24.dp.toPx() } else 0f

        val canvasModifier =
            Modifier
                .fillMaxSize()
                .padding(8.dp)
                .pointerInput(data, onElementClick, leftPadding) {
                    if (onElementClick == null) return@pointerInput
                    detectTapGestures { offset ->
                        val canvasWidth = size.width - leftPadding
                        val barCount = data.size
                        if (barCount == 0) return@detectTapGestures
                        val barSpacing = canvasWidth / barCount
                        val adjustedX = offset.x - leftPadding
                        if (adjustedX < 0) return@detectTapGestures
                        val tappedIndex = (adjustedX / barSpacing).toInt()
                        if (tappedIndex in data.indices) {
                            tapOffset = offset
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onElementClick(tappedIndex)
                        }
                    }
                }
                .pointerInput(data, onRangeSelected, leftPadding) {
                    if (onRangeSelected == null) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragStartOffset = offset.x
                            dragCurrentOffset = offset.x
                        },
                        onDragEnd = {
                            if (dragStartOffset != null && dragCurrentOffset != null) {
                                val canvasWidth = size.width - leftPadding
                                val barCount = data.size
                                if (barCount > 0) {
                                    val barSpacing = canvasWidth / barCount
                                    val startX = min(dragStartOffset!!, dragCurrentOffset!!) - leftPadding
                                    val endX = max(dragStartOffset!!, dragCurrentOffset!!) - leftPadding
                                    val startIndex = (startX / barSpacing).toInt().coerceIn(data.indices)
                                    val endIndex = (endX / barSpacing).toInt().coerceIn(data.indices)
                                    if (startIndex <= endIndex) {
                                        onRangeSelected(startIndex..endIndex)
                                    }
                                }
                            }
                            dragStartOffset = null
                            dragCurrentOffset = null
                        },
                        onDragCancel = {
                            dragStartOffset = null
                            dragCurrentOffset = null
                        },
                        onHorizontalDrag = { change, _ ->
                            dragCurrentOffset = change.position.x
                        },
                    )
                }

        val gridLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)

        Canvas(modifier = canvasModifier) {
            val canvasWidth = size.width - leftPadding
            val canvasHeight = size.height - bottomPadding

            // Draw 3 background horizontal grid lines
            val gridLineCount = 3
            for (i in 1..gridLineCount) {
                val y = (canvasHeight / (gridLineCount + 1)) * i
                val valueAtLine = (maxValue - (maxValue / (gridLineCount + 1)) * i).toInt()

                drawLine(
                    color = gridLineColor,
                    start = Offset(leftPadding, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )

                if (data.isNotEmpty()) {
                    val textLayoutResult =
                        textMeasurer.measure(
                            text = valueAtLine.toString(),
                            style = TextStyle(color = axisColor, fontSize = 10.sp),
                        )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(leftPadding - textLayoutResult.size.width - 4.dp.toPx(), y - textLayoutResult.size.height / 2f),
                    )
                }
            }

            // Draw baseline axis
            drawLine(
                color = axisColor,
                start = Offset(leftPadding, canvasHeight),
                end = Offset(size.width, canvasHeight),
                strokeWidth = 2.dp.toPx(),
            )

            if (data.isNotEmpty()) {
                val zeroLayoutResult =
                    textMeasurer.measure(
                        text = "0",
                        style = TextStyle(color = axisColor, fontSize = 10.sp),
                    )
                drawText(
                    textLayoutResult = zeroLayoutResult,
                    topLeft =
                        Offset(
                            leftPadding - zeroLayoutResult.size.width - 4.dp.toPx(),
                            canvasHeight - zeroLayoutResult.size.height / 2f,
                        ),
                )

                val firstLabel = data.first().label
                val lastLabel = data.last().label

                val firstLayout =
                    textMeasurer.measure(
                        text = firstLabel,
                        style = TextStyle(color = axisColor, fontSize = 10.sp),
                    )
                drawText(
                    textLayoutResult = firstLayout,
                    topLeft = Offset(leftPadding, canvasHeight + 8.dp.toPx()),
                )

                val lastLayout =
                    textMeasurer.measure(
                        text = lastLabel,
                        style = TextStyle(color = axisColor, fontSize = 10.sp),
                    )
                drawText(
                    textLayoutResult = lastLayout,
                    topLeft = Offset(size.width - lastLayout.size.width, canvasHeight + 8.dp.toPx()),
                )
            }

            if (dragStartOffset != null && dragCurrentOffset != null) {
                val left = min(dragStartOffset!!, dragCurrentOffset!!)
                val right = max(dragStartOffset!!, dragCurrentOffset!!)
                drawRect(
                    color = selectionColor,
                    topLeft = Offset(left, 0f),
                    size = Size(right - left, canvasHeight),
                )
            }

            if (data.isEmpty() || maxValue <= 0f) {
                return@Canvas
            }

            // Calculate layout properties
            val barCount = data.size
            val barSpacing = canvasWidth / barCount
            val maxBarWidthPx = 28.dp.toPx() // limit the maximum width of a single bar
            val computedBarWidth = (barSpacing * 0.8f).coerceAtMost(maxBarWidthPx)

            // Draw each bar
            data.forEachIndexed { index, barData ->
                val alpha = alphas.getOrElse(index) { 1.0f }
                val scale = scales.getOrElse(index) { 1.0f }

                val barHeight = (barData.value / maxValue) * canvasHeight * scale
                val scaledBarWidth = computedBarWidth * scale

                // Centering within the time bucket column
                val xOffset = leftPadding + index * barSpacing + (barSpacing - scaledBarWidth) / 2f
                val yOffset = canvasHeight - barHeight

                if (barHeight > 0f) {
                    val path =
                        Path().apply {
                            addRoundRect(
                                roundRect =
                                    RoundRect(
                                        left = xOffset,
                                        top = yOffset,
                                        right = xOffset + scaledBarWidth,
                                        bottom = canvasHeight,
                                        topLeftCornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                        topRightCornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                        bottomLeftCornerRadius = CornerRadius.Zero,
                                        bottomRightCornerRadius = CornerRadius.Zero,
                                    ),
                            )
                        }

                    // Stunning vertical gradient fade
                    val brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    barData.color.copy(alpha = alpha),
                                    barData.color.copy(alpha = alpha * 0.2f),
                                ),
                            startY = yOffset,
                            endY = canvasHeight,
                        )

                    drawPath(path, brush = brush)
                }
            }
        }

        if (selectedIndex != null && tapOffset != null && selectedIndex in data.indices) {
            val selectedData = data[selectedIndex]
            val isRightHalf = tapOffset!!.x > maxWidthPx / 2f
            val xOffset =
                if (isRightHalf) {
                    // Shift left so tooltip doesn't clip
                    with(LocalDensity.current) { (tapOffset!!.x - 120.dp.toPx()).toDp() }
                } else {
                    // Shift right slightly
                    with(LocalDensity.current) { (tapOffset!!.x + 20.dp.toPx()).toDp() }
                }
            val yOffset = with(LocalDensity.current) { (tapOffset!!.y - 30.dp.toPx()).toDp() }

            GraphTooltip(
                text = "${selectedData.value.toInt()} logs",
                modifier = Modifier.offset(x = xOffset, y = yOffset),
            )
        }
    }
}
