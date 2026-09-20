package com.spectra.logger.feature.events.model

import kotlinx.serialization.Serializable

/**
 * Categorization of logged events in the Spectra telemetry pipeline.
 */
@Serializable
enum class EventType {
    /**
     * Screen view or UI page navigation.
     */
    SCREEN_VIEW,

    /**
     * Direct user interaction such as button taps, gestures, or input submissions.
     */
    USER_ACTION,

    /**
     * Application or component lifecycle state change (e.g. foreground, background).
     */
    LIFECYCLE,

    /**
     * Custom application-defined domain event.
     */
    CUSTOM,
}
