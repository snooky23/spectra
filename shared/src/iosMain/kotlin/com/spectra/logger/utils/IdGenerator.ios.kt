package com.spectra.logger.utils

import kotlinx.atomicfu.atomic
import kotlinx.datetime.Clock

/**
 * iOS implementation using timestamp + atomic counter.
 * This provides unique IDs without UUID availability.
 */
actual object IdGenerator {
    private val counter = atomic(0)

    actual fun generate(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val count = counter.getAndIncrement()
        return "$timestamp-$count"
    }
}
