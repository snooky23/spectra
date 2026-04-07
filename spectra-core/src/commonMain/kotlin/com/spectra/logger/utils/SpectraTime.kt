package com.spectra.logger.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Common time utility to avoid resolution issues in dependent modules.
 */
object SpectraTime {
    /**
     * Returns current UTC instant.
     */
    fun now(): Instant = Clock.System.now()
}
