package com.spectra.logger.core.storage

/**
 * Policy defining storage limits and auto-pruning rules for telemetry data.
 *
 * Implements FR-2.4 (configurable retention policy) across logs, network telemetry,
 * and analytics events.
 *
 * @property maxCount Maximum number of entries to retain (oldest removed first).
 * @property maxAgeMs Maximum age in milliseconds for entries (older entries pruned).
 * @property maxSizeBytes Maximum total storage size in bytes on disk (FileLogStorage).
 */
data class RetentionPolicy(
    val maxCount: Int? = null,
    val maxAgeMs: Long? = null,
    val maxSizeBytes: Long? = null,
) {
    val hasLimits: Boolean
        get() = maxCount != null || maxAgeMs != null || maxSizeBytes != null

    companion object {
        const val ONE_HOUR_MS = 60 * 60 * 1000L
        const val ONE_DAY_MS = 24 * ONE_HOUR_MS
        const val SEVEN_DAYS_MS = 7 * ONE_DAY_MS
        const val THIRTY_DAYS_MS = 30 * ONE_DAY_MS

        val NONE = RetentionPolicy()

        val DEFAULT =
            RetentionPolicy(
                maxCount = 10_000,
                maxAgeMs = SEVEN_DAYS_MS,
            )

        val SEVEN_DAYS =
            RetentionPolicy(
                maxAgeMs = SEVEN_DAYS_MS,
            )

        val THIRTY_DAYS =
            RetentionPolicy(
                maxAgeMs = THIRTY_DAYS_MS,
            )

        val STRICT_MEMORY =
            RetentionPolicy(
                maxCount = 1_000,
                maxAgeMs = ONE_DAY_MS,
            )
    }
}
