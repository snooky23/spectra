package com.spectra.logger.feature.events.model

import com.spectra.logger.core.model.SourceType
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a discrete user interaction, screen transition, or lifecycle event.
 *
 * @property id Unique identifier for the event
 * @property timestamp When the event occurred
 * @property eventType Categorization (SCREEN_VIEW, USER_ACTION, LIFECYCLE, CUSTOM)
 * @property name Event or screen name (e.g. "HomeScreen", "checkout_tap")
 * @property parameters Arbitrary key-value metadata attached to this event
 * @property durationMs Optional elapsed duration in milliseconds (e.g. screen dwell time)
 * @property source Package/Bundle ID or file where the event was logged
 * @property sourceType Type of source (APP, SDK, PLUGIN)
 */
@Serializable
data class EventLogEntry(
    val id: String,
    val timestamp: Instant,
    val eventType: EventType,
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
    val durationMs: Long? = null,
    val source: String = "unknown",
    val sourceType: SourceType = SourceType.APP,
)
