package com.spectra.logger.core.ui.util

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

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
