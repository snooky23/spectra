package com.spectra.logger.utils

import java.util.UUID

/**
 * Android implementation using UUID.
 */
actual object IdGenerator {
    actual fun generate(): String = UUID.randomUUID().toString()
}
