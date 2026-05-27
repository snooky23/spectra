package com.spectra.logger.core.utils

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import kotlinx.atomicfu.atomic

/**
 * iOS implementation using timestamp + atomic counter.
 * This provides unique IDs without UUID availability.
 */
actual object IdGenerator {
    private val counter = atomic(0)

    actual fun generate(): String {
        val timestamp = com.spectra.logger.core.utils.SpectraTime.now().toEpochMilliseconds()
        val count = counter.getAndIncrement()
        return "$timestamp-$count"
    }
}
