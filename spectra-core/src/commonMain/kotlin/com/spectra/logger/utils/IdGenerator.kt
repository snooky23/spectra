package com.spectra.logger.utils

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
