package com.spectra.logger.ui.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import kotlin.math.min
import kotlin.math.max
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.spectra.logger.ui.compose.model.BarChartData
import kotlinx.collections.immutable.ImmutableList

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

    val alphas = data.indices.map { index ->
        val isSelected = selectedIndex == index
        val hasSelection = selectedIndex != null
        animateFloatAsState(
            targetValue = if (hasSelection && !isSelected) 0.4f else 1.0f,
            label = "alpha_$index"
        ).value
    }

    val scales = data.indices.map { index ->
        val isSelected = selectedIndex == index
        animateFloatAsState(
            targetValue = if (isSelected) 1.05f else 1.0f,
            label = "scale_$index"
        ).value
    }

    var tapOffset by remember { mutableStateOf<Offset?>(null) }
    if (selectedIndex == null) {
        tapOffset = null
    }

    var dragStartOffset by remember { mutableStateOf<Float?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Float?>(null) }

    BoxWithConstraints(modifier = baseModifier.fillMaxSize()) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        
        val canvasModifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .pointerInput(data, onElementClick) {
                if (onElementClick == null) return@pointerInput
                detectTapGestures { offset ->
                    val canvasWidth = size.width
                    val barCount = data.size
                    if (barCount == 0) return@detectTapGestures
                    val barSpacing = canvasWidth / barCount
                    val tappedIndex = (offset.x / barSpacing).toInt()
                    if (tappedIndex in data.indices) {
                        tapOffset = offset
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onElementClick(tappedIndex)
                    }
                }
            }
            .pointerInput(data, onRangeSelected) {
                if (onRangeSelected == null) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragStartOffset = offset.x
                        dragCurrentOffset = offset.x
                    },
                    onDragEnd = {
                        if (dragStartOffset != null && dragCurrentOffset != null) {
                            val canvasWidth = size.width
                            val barCount = data.size
                            if (barCount > 0) {
                                val barSpacing = canvasWidth / barCount
                                val startX = min(dragStartOffset!!, dragCurrentOffset!!)
                                val endX = max(dragStartOffset!!, dragCurrentOffset!!)
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
                    }
                )
            }

        Canvas(modifier = canvasModifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw baseline axis
        drawLine(
            color = axisColor,
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, canvasHeight),
            strokeWidth = 2.dp.toPx(),
        )

        if (dragStartOffset != null && dragCurrentOffset != null) {
            val left = min(dragStartOffset!!, dragCurrentOffset!!)
            val right = max(dragStartOffset!!, dragCurrentOffset!!)
            drawRect(
                color = selectionColor,
                topLeft = Offset(left, 0f),
                size = Size(right - left, canvasHeight)
            )
        }

        if (data.isEmpty() || maxValue <= 0f) {
            return@Canvas
        }

        // Calculate layout properties
        val barCount = data.size
        // Fixed gap ratio: 20% of the bar spacing is gap, 80% is the bar
        val barSpacing = canvasWidth / barCount
        val baseBarWidth = barSpacing * 0.8f
        val gapWidth = barSpacing * 0.2f

        // Draw each bar
        data.forEachIndexed { index, barData ->
            val alpha = alphas.getOrElse(index) { 1.0f }
            val scale = scales.getOrElse(index) { 1.0f }
            
            val barHeight = (barData.value / maxValue) * canvasHeight * scale
            val barWidth = baseBarWidth * scale
            
            // Adjust xOffset slightly if scaled to keep it centered
            val widthDiff = barWidth - baseBarWidth
            val xOffset = index * barSpacing + (gapWidth / 2f) - (widthDiff / 2f)
            val yOffset = canvasHeight - barHeight

            if (barHeight > 0f) {
                drawRoundRect(
                    color = barData.color.copy(alpha = alpha),
                    topLeft = Offset(xOffset, yOffset),
                    size = Size(barWidth, barHeight),
                    // Slight rounding for premium feel
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }
        }
    }

    if (selectedIndex != null && tapOffset != null && selectedIndex in data.indices) {
        val selectedData = data[selectedIndex]
        val isRightHalf = tapOffset!!.x > maxWidthPx / 2f
        val xOffset = if (isRightHalf) {
            // Shift left so tooltip doesn't clip
            with(LocalDensity.current) { (tapOffset!!.x - 120.dp.toPx()).toDp() }
        } else {
            // Shift right slightly
            with(LocalDensity.current) { (tapOffset!!.x + 20.dp.toPx()).toDp() }
        }
        val yOffset = with(LocalDensity.current) { (tapOffset!!.y - 30.dp.toPx()).toDp() }
        
        GraphTooltip(
            text = "${selectedData.value.toInt()} logs",
            modifier = Modifier.offset(x = xOffset, y = yOffset)
        )
    }
}
}
