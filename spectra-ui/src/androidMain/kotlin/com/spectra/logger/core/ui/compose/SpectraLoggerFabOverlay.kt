package com.spectra.logger.core.ui.compose

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.spectra.logger.core.ui.SpectraUIManager
import kotlin.math.roundToInt

/**
 * A draggable Floating Action Button (FAB) overlay that launches the Spectra Logger UI.
 *
 * The logger UI is rendered as a full-screen in-window overlay (not a Dialog), which
 * guarantees that WindowInsets (navigation bar, status bar) are correctly dispatched
 * from the host Activity. This means the NavigationBar pads itself properly for the
 * gesture area without any workarounds.
 *
 * Wrap your root app composable with this to get easy debug access.
 */
@Composable
fun SpectraLoggerFabOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isShowing by SpectraUIManager.isShowing.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        content()

        // Full-screen in-window overlay — rendered in the same Activity window so
        // WindowInsets from enableEdgeToEdge() are dispatched correctly.
        AnimatedVisibility(
            visible = isShowing,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                SpectraLoggerScreen(onDismiss = { SpectraUIManager.dismissScreen() })
            }
        }

        // FAB is only shown when the logger UI is hidden
        if (enabled && !isShowing) {
            DraggableLoggerFab()
        }
    }
}

/**
 * Alternative modal presentation of the Spectra Logger UI using a Dialog.
 *
 * Use this if you need the logger to appear as a modal overlay on top of a separate
 * window (e.g. inside another Dialog). This properly configures edge-to-edge insets
 * on the Dialog's own window via [WindowCompat.setDecorFitsSystemWindows] so that
 * the bottom navigation bar is visible above the gesture area.
 */
@Composable
fun SpectraLoggerDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        // Access the Dialog's own Window and call WindowCompat to properly enable
        // edge-to-edge inset dispatch. DialogProperties.decorFitsSystemWindows only
        // sets the old View flag — WindowCompat is the compat API that makes the
        // system actually dispatch insets into the composition tree.
        val dialogView = LocalView.current
        val dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            SpectraLoggerScreen(onDismiss = onDismiss)
        }
    }
}

@Composable
private fun DraggableLoggerFab(modifier: Modifier = Modifier) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var fabSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val spacing = with(density) { 16.dp.toPx() }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    containerSize = coordinates.size
                },
    ) {
        FloatingActionButton(
            onClick = { SpectraUIManager.showScreen() },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier =
                modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .onGloballyPositioned { coordinates ->
                        fabSize = coordinates.size
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            val newX = offsetX + dragAmount.x
                            val newY = offsetY + dragAmount.y

                            val maxX = 0f
                            val minX = -(containerSize.width.toFloat() - fabSize.width.toFloat() - (spacing * 2))
                            val maxY = 0f
                            val minY = -(containerSize.height.toFloat() - fabSize.height.toFloat() - (spacing * 2))

                            offsetX = newX.coerceIn(minX, maxX)
                            offsetY = newY.coerceIn(minY, maxY)
                        }
                    },
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "Open Spectra Logger",
            )
        }
    }
}
