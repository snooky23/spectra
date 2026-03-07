package com.spectra.logger.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.spectra.logger.ui.SpectraUIManager
import kotlin.math.roundToInt

/**
 * A draggable Floating Action Button (FAB) overlay that launches the Spectra Logger UI.
 * Wrap your root app composable with this to get easy debug access.
 *
 * Example:
 * ```
 * setContent {
 *     SpectraLoggerFabOverlay(enabled = BuildConfig.DEBUG) {
 *         MyApp()
 *     }
 * }
 * ```
 *
 * @param modifier The modifier to be applied to the layout.
 * @param enabled Whether the FAB should be shown. Tip: tie this to a Debug/Feature flag.
 * @param content The main application content to be displayed underneath.
 */
@Composable
fun SpectraLoggerFabOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (enabled) {
            DraggableLoggerFab()
        }
    }
}

@Composable
private fun DraggableLoggerFab(
    modifier: Modifier = Modifier,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var fabSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val spacing = with(density) { 16.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                containerSize = coordinates.size
            }
    ) {
        FloatingActionButton(
            onClick = { SpectraUIManager.showScreen() },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .onGloballyPositioned { coordinates ->
                    fabSize = coordinates.size
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        
                        // Calculate new raw position
                        val newX = offsetX + dragAmount.x
                        val newY = offsetY + dragAmount.y
                        
                        // Limit dragging to window bounds
                        val maxX = 0f
                        val minX = -(containerSize.width - fabSize.width - (spacing * 2))
                        val maxY = 0f
                        val minY = -(containerSize.height - fabSize.height - (spacing * 2))

                        offsetX = newX.coerceIn(minX, maxX)
                        offsetY = newY.coerceIn(minY, maxY)
                    }
                }
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "Open Spectra Logger"
            )
        }
    }
}
