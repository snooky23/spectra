package com.spectra.logger.feature.logs.model

import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
import kotlinx.serialization.Serializable

/**
 * Severity levels for log entries.
 * Ordered from least to most severe.
 */
@Serializable
enum class LogLevel(val priority: Int) {
    VERBOSE(0),
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    FATAL(5),
    ;

    companion object {
        fun fromPriority(priority: Int): LogLevel? = entries.find { it.priority == priority }
    }
}
