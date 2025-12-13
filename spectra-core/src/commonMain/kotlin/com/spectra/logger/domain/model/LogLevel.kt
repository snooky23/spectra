package com.spectra.logger.domain.model

/**
 * Severity levels for log entries.
 * Ordered from least to most severe.
 */
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
