package com.spectra.logger.feature.events.ui

import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType

/**
 * UI State for the Events Screen.
 */
data class EventsUiState(
    val events: List<EventLogEntry> = emptyList(),
    val filteredEvents: List<EventLogEntry> = emptyList(),
    val selectedEventTypes: Set<EventType> = emptySet(),
    val searchText: String = "",
    val minDurationMs: Long? = null,
    val isLoading: Boolean = false,
) {
    val advancedFilter: EventFilter
        get() =
            EventFilter(
                eventTypes = if (selectedEventTypes.isEmpty()) null else selectedEventTypes,
                searchText = if (searchText.isBlank()) null else searchText,
                minDurationMs = minDurationMs,
            )
}
