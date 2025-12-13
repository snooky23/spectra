package com.spectra.logger.domain.model

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
