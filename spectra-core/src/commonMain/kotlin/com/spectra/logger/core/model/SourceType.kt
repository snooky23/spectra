package com.spectra.logger.core.model

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import kotlinx.serialization.Serializable

/**
 * Represents the type of source that generated a log or network entry.
 *
 * Used to differentiate between logs from the main application, SDKs, plugins, etc.
 */
@Serializable
enum class SourceType {
    /**
     * Log originated from the main application
     */
    APP,

    /**
     * Log originated from an SDK or library
     */
    SDK,

    /**
     * Log originated from a plugin or extension
     */
    PLUGIN,
}
