package com.spectra.logger.ui.util

/**
 * Platform-specific utilities for the UI module.
 */
expect object PlatformUtils {
    /**
     * Share text using the platform's native share sheet.
     */
    fun shareText(
        text: String,
        title: String,
        context: Any? = null,
    )
}
