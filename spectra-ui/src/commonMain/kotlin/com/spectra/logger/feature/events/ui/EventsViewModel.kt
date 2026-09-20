package com.spectra.logger.feature.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.events.storage.EventLogStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing state and real-time observation for the Events tab.
 *
 * Implements Clean Architecture with Dependency Inversion: accepts [storage]
 * via constructor injection, defaulting to [SpectraLogger.eventStorage].
 */
class EventsViewModel(
    private val storage: EventLogStorage = SpectraLogger.eventStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
        observeLiveEvents()
    }

    private fun observeLiveEvents() {
        viewModelScope.launch {
            storage.observe().collect { newEntry ->
                _uiState.update { state ->
                    val newEvents = listOf(newEntry) + state.events
                    state.copy(events = newEvents)
                }
                applyFilters()
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val events = storage.query(filter = EventFilter.NONE, limit = null)
                _uiState.update { state ->
                    state.copy(
                        events = events,
                        isLoading = false,
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        applyFilters()
    }

    fun toggleEventType(type: EventType) {
        _uiState.update { state ->
            val newTypes =
                if (state.selectedEventTypes.contains(type)) {
                    state.selectedEventTypes - type
                } else {
                    state.selectedEventTypes + type
                }
            state.copy(selectedEventTypes = newTypes)
        }
        applyFilters()
    }

    fun updateFilter(filter: EventFilter) {
        _uiState.update { state ->
            state.copy(
                selectedEventTypes = filter.eventTypes ?: emptySet(),
                searchText = filter.searchText ?: state.searchText,
                minDurationMs = filter.minDurationMs,
            )
        }
        applyFilters()
    }

    fun clearEvents() {
        viewModelScope.launch {
            storage.clear()
            _uiState.update { state ->
                state.copy(
                    events = emptyList(),
                    filteredEvents = emptyList(),
                )
            }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filter = state.advancedFilter
        val filtered = state.events.filter { filter.matches(it) }
        _uiState.update { it.copy(filteredEvents = filtered) }
    }
}
