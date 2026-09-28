package com.spectra.logger.core.ui.theme

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

/**
 * Holds the structural layout context based on available physical window space.
 */
data class ScreenConfig(
    val isCompact: Boolean,
    val isDualPane: Boolean,
)

/**
 * Retrieves the current layout context, automatically recalculating upon orientation or window change.
 */
@Composable
fun rememberScreenConfig(): ScreenConfig {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isDualPane = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    return ScreenConfig(
        isCompact = !isDualPane,
        isDualPane = isDualPane,
    )
}
