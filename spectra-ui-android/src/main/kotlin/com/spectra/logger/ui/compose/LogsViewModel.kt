package com.spectra.logger.ui.compose

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.LogFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * ViewModel for the Logs screen
 */
class LogsViewModel : ViewModel() {
    
    private val storage = SpectraLogger.logStorage
    
    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val noFilter = LogFilter(
                    levels = null,
                    tags = null,
                    searchText = null,
                    fromTimestamp = null,
                    toTimestamp = null
                )
                val logs = storage.query(filter = noFilter, limit = null)
                val availableTags = logs.map { it.tag }.distinct().sorted()
                
                _uiState.update { state ->
                    state.copy(
                        logs = logs,
                        availableTags = availableTags,
                        isLoading = false
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

    fun toggleLevel(level: LogLevel) {
        _uiState.update { state ->
            val newLevels = if (state.selectedLevels.contains(level)) {
                state.selectedLevels - level
            } else {
                state.selectedLevels + level
            }
            state.copy(selectedLevels = newLevels)
        }
        applyFilters()
    }
    
    fun updateLevels(levels: Set<LogLevel>) {
        _uiState.update { it.copy(selectedLevels = levels) }
        applyFilters()
    }

    fun updateFilter(filter: AdvancedFilter) {
        _uiState.update { it.copy(advancedFilter = filter) }
        applyFilters()
    }

    fun removeTagFilter(tag: String) {
        _uiState.update { state ->
            state.copy(
                advancedFilter = state.advancedFilter.copy(
                    selectedTags = state.advancedFilter.selectedTags - tag,
                    customTags = state.advancedFilter.customTags - tag
                )
            )
        }
        applyFilters()
    }

    fun clearTimeRangeFilter() {
        _uiState.update { state ->
            state.copy(
                advancedFilter = state.advancedFilter.copy(
                    fromTimestamp = null,
                    toTimestamp = null
                )
            )
        }
        applyFilters()
    }

    fun clearHasErrorFilter() {
        _uiState.update { state ->
            state.copy(
                advancedFilter = state.advancedFilter.copy(hasErrorOnly = false)
            )
        }
        applyFilters()
    }

    fun clearLogs() {
        viewModelScope.launch {
            storage.clear()
            _uiState.update {
                it.copy(logs = emptyList(), filteredLogs = emptyList(), availableTags = emptyList())
            }
        }
    }
    
    fun shareLogs(logs: List<LogEntry>) {
        // TODO: Implement actual sharing via Intent
        // This would require context access, typically done via Application class or passed in
        val logsText = logs.joinToString("\n") { log ->
            "[${log.level.name}] ${log.timestamp} - ${log.tag}: ${log.message}"
        }
        // For now, we just prepare the text. Actual sharing would be done in the UI layer.
        println("Share logs: $logsText")
    }

    private fun applyFilters() {
        _uiState.update { state ->
            var filtered = state.logs

            // Filter by log level
            if (state.selectedLevels.isNotEmpty()) {
                filtered = filtered.filter { state.selectedLevels.contains(it.level) }
            }

            // Filter by tags
            val allTags = state.advancedFilter.allSelectedTags
            if (allTags.isNotEmpty()) {
                filtered = filtered.filter { allTags.contains(it.tag) }
            }

            // Filter by time range
            state.advancedFilter.fromTimestamp?.let { from ->
                filtered = filtered.filter { it.timestamp >= from }
            }
            state.advancedFilter.toTimestamp?.let { to ->
                filtered = filtered.filter { it.timestamp <= to }
            }

            // Filter by has error
            if (state.advancedFilter.hasErrorOnly) {
                filtered = filtered.filter { 
                    it.throwable != null || it.metadata.containsKey("stack_trace")
                }
            }

            // Filter by search text (min 2 chars)
            if (state.searchText.length >= 2) {
                val query = state.searchText.lowercase()
                filtered = filtered.filter { log ->
                    log.message.lowercase().contains(query) ||
                    log.tag.lowercase().contains(query) ||
                    log.level.name.lowercase().contains(query)
                }
            }

            state.copy(filteredLogs = filtered)
        }
    }
}

/**
 * UI State for Logs screen
 */
data class LogsUiState(
    val logs: List<LogEntry> = emptyList(),
    val filteredLogs: List<LogEntry> = emptyList(),
    val isLoading: Boolean = true,
    val searchText: String = "",
    val selectedLevels: Set<LogLevel> = emptySet(),
    val availableTags: List<String> = emptyList(),
    val advancedFilter: AdvancedFilter = AdvancedFilter()
) {
    val hasActiveFilters: Boolean
        get() = advancedFilter.hasActiveFilters
    
    val hasAnyActiveFilters: Boolean
        get() = selectedLevels.isNotEmpty() || advancedFilter.hasActiveFilters
    
    val activeFilterCount: Int
        get() = advancedFilter.activeFilterCount
    
    val totalActiveFilterCount: Int
        get() = selectedLevels.size + advancedFilter.activeFilterCount
    
    val selectedTags: Set<String>
        get() = advancedFilter.allSelectedTags
    
    val hasTimeRangeFilter: Boolean
        get() = advancedFilter.fromTimestamp != null || advancedFilter.toTimestamp != null
    
    val hasErrorOnly: Boolean
        get() = advancedFilter.hasErrorOnly
}

/**
 * Advanced filter configuration
 */
data class AdvancedFilter(
    val selectedTags: Set<String> = emptySet(),
    val customTags: Set<String> = emptySet(),
    val fromTimestamp: Instant? = null,
    val toTimestamp: Instant? = null,
    val metadataKey: String = "",
    val metadataValue: String = "",
    val hasErrorOnly: Boolean = false
) {
    val allSelectedTags: Set<String>
        get() = selectedTags + customTags
    
    val hasActiveFilters: Boolean
        get() = selectedTags.isNotEmpty() ||
                customTags.isNotEmpty() ||
                fromTimestamp != null ||
                toTimestamp != null ||
                (metadataKey.isNotEmpty() && metadataValue.isNotEmpty()) ||
                hasErrorOnly
    
    val activeFilterCount: Int
        get() {
            var count = 0
            if (allSelectedTags.isNotEmpty()) count++
            if (fromTimestamp != null || toTimestamp != null) count++
            if (metadataKey.isNotEmpty() && metadataValue.isNotEmpty()) count++
            if (hasErrorOnly) count++
            return count
        }
}
