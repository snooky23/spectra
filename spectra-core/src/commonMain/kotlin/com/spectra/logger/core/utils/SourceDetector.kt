package com.spectra.logger.core.utils

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*


/**
 * Detects the source (origin) of a log or network request automatically
 * by analyzing the call stack.
 *
 * This allows differentiation between logs from the main app vs SDKs
 * without requiring explicit configuration by developers.
 */
expect object SourceDetector {
    /**
     * Detects the source package/bundle ID from the call stack.
     *
     * Analyzes stack frames to identify which package/library made the call.
     *
     * @return A pair of (sourceId, sourceType) where sourceId is the package name
     * and sourceType indicates whether it's APP, SDK, or PLUGIN
     */
    fun detectSource(): Pair<String, SourceType>
}
