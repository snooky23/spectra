package com.spectra.logger.core.utils

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import java.util.UUID

/**
 * Android implementation using UUID.
 */
actual object IdGenerator {
    actual fun generate(): String = UUID.randomUUID().toString()
}
