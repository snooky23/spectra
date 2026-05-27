package com.spectra.logger.core.utils

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

/**
 * Platform-specific ID generator for log entries.
 */
expect object IdGenerator {
    /**
     * Generate a unique ID string.
     * Implementation should be thread-safe and produce unique IDs.
     */
    fun generate(): String
}
