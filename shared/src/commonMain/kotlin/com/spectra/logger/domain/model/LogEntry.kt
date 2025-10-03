package com.spectra.logger.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a single log entry in the system.
 *
 * @property id Unique identifier for the log entry
 * @property timestamp When the log was created
 * @property level Severity level
 * @property tag Category or source of the log
 * @property message Log message content
 * @property throwable Optional exception/error details
 * @property metadata Additional key-value data
 */
data class LogEntry(
    val id: String,
    val timestamp: Instant,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)
