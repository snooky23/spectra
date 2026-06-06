package com.spectra.logger.core.utils

import com.spectra.logger.core.model.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Platform-independent ID generator for log entries.
 */
@OptIn(ExperimentalUuidApi::class)
object IdGenerator {
    /**
     * Generate a unique ID string.
     * Implementation should be thread-safe and produce unique IDs.
     */
    fun generate(): String = Uuid.random().toString()
}
