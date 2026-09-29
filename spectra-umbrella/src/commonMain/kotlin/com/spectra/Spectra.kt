package com.spectra

import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.settings.config.LoggerConfigurationBuilder

/**
 * Unified entry point and top-level facade for the Spectra SDK.
 *
 * Provides convenient access to the core [SpectraLogger] instance and configuration.
 */
object Spectra {
    /**
     * The shared [SpectraLogger] instance.
     */
    val logger: SpectraLogger
        get() = SpectraLogger

    /**
     * Current library version.
     */
    val version: String
        get() = SpectraLogger.getVersion()

    /**
     * Configures the Spectra SDK with the specified [block].
     */
    fun configure(block: LoggerConfigurationBuilder.() -> Unit) {
        SpectraLogger.configure(block)
    }

    /**
     * Records a telemetry event with the given [name], [parameters], [eventType], and optional [durationMs].
     */
    fun event(
        name: String,
        parameters: Map<String, String> = emptyMap(),
        eventType: EventType = EventType.USER_ACTION,
        durationMs: Long? = null,
    ) {
        SpectraLogger.event(
            name = name,
            parameters = parameters,
            eventType = eventType,
            durationMs = durationMs,
        )
    }
}
