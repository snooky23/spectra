package com.spectra.logger.utils

import kotlinx.atomicfu.atomic

/**
 * iOS implementation using timestamp + atomic counter.
 * This provides unique IDs without UUID availability.
 */
actual object IdGenerator {
    private val counter = atomic(0)

    actual fun generate(): String {
        val timestamp = com.spectra.logger.utils.SpectraTime.now().toEpochMilliseconds()
        val count = counter.getAndIncrement()
        return "$timestamp-$count"
    }
}
