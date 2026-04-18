package com.spectra.logger.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Hardcoded UI Design Tokens for Spectra Logger.
 * Defines Exact hex colors and UI structural choices based on UX specifications.
 */
object SpectraDesignTokens {
    // Log Levels
    val VerboseGray = Color(0xFF8E8E93)
    val DebugBlue = Color(0xFF007AFF)
    val InfoGreen = Color(0xFF34C759)
    val WarningOrange = Color(0xFFFF9500)
    val ErrorRed = Color(0xFFFF3B30)
    val FatalPurple = Color(0xFFAF52DE)

    // UI Component Backgrounds
    val SystemGray6 = Color(0xFFF2F2F7)

    // UI Component Structurals
    val SearchBarShape = RoundedCornerShape(10.dp)
    val DetailCardShape = RoundedCornerShape(8.dp)
    const val FilterChipAlpha = 0.2f

    // Responsive Layout Padding
    val ScreenHorizontalPaddingCompact = 16.dp
    val ScreenHorizontalPaddingExpanded = 24.dp

    // Dual-Pane Divider
    val ListToDetailGap = 1.dp
}
