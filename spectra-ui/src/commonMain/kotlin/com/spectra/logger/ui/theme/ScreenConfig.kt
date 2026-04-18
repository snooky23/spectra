package com.spectra.logger.ui.theme

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * Holds the structural layout context based on available physical window space.
 */
data class ScreenConfig(
    val isCompact: Boolean,
    val isDualPane: Boolean
)

/**
 * Retrieves the current layout context, automatically recalculating upon orientation or window change.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberScreenConfig(): ScreenConfig {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val widthSizeClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass
    
    val isCompact = widthSizeClass == WindowWidthSizeClass.COMPACT
    val isDualPane = widthSizeClass == WindowWidthSizeClass.MEDIUM || 
                     widthSizeClass == WindowWidthSizeClass.EXPANDED
                     
    return ScreenConfig(
        isCompact = isCompact,
        isDualPane = isDualPane
    )
}
