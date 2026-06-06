package com.spectra.logger.core.ui.util

import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*

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
