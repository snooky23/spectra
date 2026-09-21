package com.spectra.logger.feature.crash.model

import com.spectra.logger.core.utils.SpectraTime
import kotlinx.serialization.Serializable

@Serializable
enum class BreadcrumbType {
    LOG,
    NETWORK,
    EVENT,
    USER_ACTION,
    SYSTEM,
}

@Serializable
data class Breadcrumb(
    val timestamp: Long = SpectraTime.now().toEpochMilliseconds(),
    val type: BreadcrumbType,
    val category: String,
    val message: String,
    val data: Map<String, String> = emptyMap(),
)
