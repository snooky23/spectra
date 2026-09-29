package com.spectra

import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.crash.model.BreadcrumbType
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.settings.config.LoggerConfigurationBuilder

/**
 * Unified entry point and top-level facade for the Spectra SDK.
 *
 * Provides convenient access to the core [SpectraLogger] instance, logging methods,
 * event tracking, crash telemetry, and configuration.
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
     * Log a message at VERBOSE level.
     */
    fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        SpectraLogger.v(tag, message, throwable, metadata)
    }

    /**
     * Log a message at DEBUG level.
     */
    fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        SpectraLogger.d(tag, message, throwable, metadata)
    }

    /**
     * Log a message at INFO level.
     */
    fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        SpectraLogger.i(tag, message, throwable, metadata)
    }

    /**
     * Log a message at WARN level.
     */
    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        SpectraLogger.w(tag, message, throwable, metadata)
    }

    /**
     * Log a message at ERROR level.
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        SpectraLogger.e(tag, message, throwable, metadata)
    }

    /**
     * Log a message at FATAL level.
     */
    fun f(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String>? = null,
    ) {
        SpectraLogger.f(tag, message, throwable, metadata)
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

    /**
     * Marks the start of a screen transition or view session.
     */
    fun screenStart(
        screenName: String,
        parameters: Map<String, String> = emptyMap(),
    ) {
        SpectraLogger.screenStart(screenName, parameters)
    }

    /**
     * Marks the end of a screen transition or view session.
     */
    fun screenEnd(
        screenName: String,
        additionalParameters: Map<String, String> = emptyMap(),
    ) {
        SpectraLogger.screenEnd(screenName, additionalParameters)
    }

    /**
     * Records a manual breadcrumb leading up to any potential failure.
     */
    fun recordBreadcrumb(
        type: BreadcrumbType,
        category: String,
        message: String,
        data: Map<String, String> = emptyMap(),
    ) {
        SpectraLogger.recordBreadcrumb(type, category, message, data)
    }

    /**
     * Records a non-fatal exception without terminating the application.
     */
    fun recordNonFatalException(
        throwable: Throwable,
        metadata: Map<String, String> = emptyMap(),
    ) {
        SpectraLogger.recordNonFatalException(throwable, metadata)
    }
}
