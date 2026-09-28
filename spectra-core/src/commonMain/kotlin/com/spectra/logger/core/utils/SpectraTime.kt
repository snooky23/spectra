package com.spectra.logger.core.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Common time utility to avoid resolution issues in dependent modules.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
object SpectraTime {
    /**
     * Returns current UTC instant.
     */
    fun now(): Instant = Clock.System.now()

    /**
     * Returns an instant from the specified number of hours ago.
     */
    fun hoursAgo(count: Int): Instant = Clock.System.now().minus(count.hours)

    /**
     * Returns an instant from the specified number of days ago.
     */
    fun daysAgo(count: Int): Instant = Clock.System.now().minus(count.days)
}

