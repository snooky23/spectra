package com.spectra.logger.core.ui.components.effects

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.spectra.logger.core.model.*
import com.spectra.logger.core.ui.components.charts.*
import com.spectra.logger.core.ui.components.common.*
import com.spectra.logger.core.ui.components.pickers.*
import com.spectra.logger.core.utils.*

/**
 * A standard, reusable shimmer effect modifier to simulate loading states across the application.
 * Uses MaterialTheme.colorScheme.onSurface with varying alphas for the gradient.
 */
fun Modifier.shimmerEffect(): Modifier =
    composed {
        val shimmerColors =
            listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            )

        val transition = rememberInfiniteTransition()
        val translateAnim =
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                durationMillis = 1200,
                                easing = FastOutSlowInEasing,
                            ),
                        repeatMode = RepeatMode.Restart,
                    ),
            )

        this.then(
            Modifier.drawBehind {
                val brush =
                    Brush.linearGradient(
                        colors = shimmerColors,
                        start = Offset.Zero,
                        end = Offset(x = translateAnim.value, y = translateAnim.value),
                    )
                drawRect(brush = brush)
            },
        )
    }
