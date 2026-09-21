package com.spectra.logger.feature.crash.model

import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import kotlinx.serialization.Serializable

@Serializable
enum class CrashSeverity {
    FATAL,
    NON_FATAL,
}

@Serializable
data class CrashReport(
    val id: String = IdGenerator.generate(),
    val timestamp: Long = SpectraTime.now().toEpochMilliseconds(),
    val exceptionClass: String,
    val message: String? = null,
    val stackTrace: String,
    val threadName: String = "main",
    val severity: CrashSeverity = CrashSeverity.FATAL,
    val breadcrumbs: List<Breadcrumb> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
