package com.spectra.logger.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.NetworkLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URL

/**
 * ViewModel for the Network Logs screen
 */
class NetworkLogsViewModel : ViewModel() {
    private val storage = SpectraLogger.networkStorage

    private val _uiState = MutableStateFlow(NetworkLogsUiState())
    val uiState: StateFlow<NetworkLogsUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val logs = storage.query()
                val availableMethods = logs.map { log -> log.method }.distinct().sorted()

                _uiState.update { state ->
                    state.copy(
                        logs = logs,
                        availableMethods = availableMethods,
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

    fun toggleMethod(method: String) {
        _uiState.update { state ->
            val newMethods =
                if (state.selectedMethods.contains(method)) {
                    state.selectedMethods - method
                } else {
                    state.selectedMethods + method
                }
            state.copy(selectedMethods = newMethods)
        }
        applyFilters()
    }

    fun toggleStatusRange(range: String) {
        _uiState.update { state ->
            val newRanges =
                if (state.selectedStatusRanges.contains(range)) {
                    state.selectedStatusRanges - range
                } else {
                    state.selectedStatusRanges + range
                }
            state.copy(selectedStatusRanges = newRanges)
        }
        applyFilters()
    }

    fun updateAdvancedFilter(filter: NetworkFilterConfig) {
        _uiState.update { it.copy(advancedFilter = filter) }
        applyFilters()
    }

    fun removeMethodFilter(method: String) {
        _uiState.update { state ->
            state.copy(selectedMethods = state.selectedMethods - method)
        }
        applyFilters()
    }

    fun removeStatusRangeFilter(range: String) {
        _uiState.update { state ->
            state.copy(selectedStatusRanges = state.selectedStatusRanges - range)
        }
        applyFilters()
    }

    fun clearHostFilter() {
        _uiState.update { state ->
            state.copy(advancedFilter = state.advancedFilter.copy(hostPattern = ""))
        }
        applyFilters()
    }

    fun clearTimeRangeFilter() {
        _uiState.update { state ->
            state.copy(
                advancedFilter =
                    state.advancedFilter.copy(
                        fromTimestamp = null,
                        toTimestamp = null,
                    ),
            )
        }
        applyFilters()
    }

    fun clearResponseTimeFilter() {
        _uiState.update { state ->
            state.copy(advancedFilter = state.advancedFilter.copy(responseTimeThreshold = null))
        }
        applyFilters()
    }

    fun clearFailedOnlyFilter() {
        _uiState.update { state ->
            state.copy(advancedFilter = state.advancedFilter.copy(showOnlyFailed = false))
        }
        applyFilters()
    }

    fun clearLogs() {
        viewModelScope.launch {
            storage.clear()
            _uiState.update {
                it.copy(logs = emptyList(), filteredLogs = emptyList(), availableMethods = emptyList())
            }
        }
    }

    private fun applyFilters() {
        _uiState.update { state ->
            var filtered = state.logs
            val advancedFilter = state.advancedFilter

            // Combine inline with advanced filter
            val methodsToFilter =
                if (state.selectedMethods.isNotEmpty()) {
                    state.selectedMethods
                } else {
                    advancedFilter.selectedMethods
                }
            val statusRangesToFilter =
                if (state.selectedStatusRanges.isNotEmpty()) {
                    state.selectedStatusRanges
                } else {
                    advancedFilter.selectedStatusRanges
                }

            // Filter by method
            if (methodsToFilter.isNotEmpty()) {
                filtered = filtered.filter { methodsToFilter.contains(it.method) }
            }

            // Filter by status code range
            if (statusRangesToFilter.isNotEmpty()) {
                filtered =
                    filtered.filter { log ->
                        val status = log.responseCode ?: return@filter false
                        statusRangesToFilter.any { range ->
                            when (range) {
                                "2xx" -> status in 200..299
                                "3xx" -> status in 300..399
                                "4xx" -> status in 400..499
                                "5xx" -> status in 500..599
                                else -> false
                            }
                        }
                    }
            }

            // Filter by search text
            if (state.searchText.isNotEmpty()) {
                val query = state.searchText.lowercase()
                filtered =
                    filtered.filter { log ->
                        log.url.lowercase().contains(query) ||
                            log.method.lowercase().contains(query)
                    }
            }

            // Filter by host pattern
            if (advancedFilter.hostPattern.isNotEmpty()) {
                val pattern = advancedFilter.hostPattern.lowercase()
                filtered =
                    filtered.filter { log ->
                        val host = extractHost(log.url).lowercase()
                        matchesWildcard(pattern, host)
                    }
            }

            // Filter by time range
            advancedFilter.fromTimestamp?.let { from ->
                filtered = filtered.filter { log -> log.timestamp >= from }
            }
            advancedFilter.toTimestamp?.let { to ->
                filtered = filtered.filter { log -> log.timestamp <= to }
            }

            // Filter by response time threshold
            advancedFilter.responseTimeThreshold?.let { threshold ->
                filtered = filtered.filter { log -> log.duration >= threshold.milliseconds }
            }

            // Filter by failed requests only
            if (advancedFilter.showOnlyFailed) {
                filtered =
                    filtered.filter { log ->
                        log.error?.isNotEmpty() == true ||
                            (log.responseCode?.let { it >= 400 } ?: false)
                    }
            }

            state.copy(filteredLogs = filtered)
        }
    }

    private fun extractHost(url: String): String =
        try {
            URL(url).host ?: url
        } catch (e: Exception) {
            url
        }

    private fun matchesWildcard(
        pattern: String,
        text: String,
    ): Boolean {
        if (pattern.isEmpty()) return true
        if (pattern == "*") return true

        return when {
            pattern.startsWith("*") && pattern.endsWith("*") -> {
                val middle = pattern.drop(1).dropLast(1)
                text.contains(middle)
            }
            pattern.startsWith("*") -> {
                val suffix = pattern.drop(1)
                text.endsWith(suffix)
            }
            pattern.endsWith("*") -> {
                val prefix = pattern.dropLast(1)
                text.startsWith(prefix)
            }
            pattern.contains("*") -> {
                val parts = pattern.split("*")
                var remaining = text
                parts.all { part ->
                    val index = remaining.indexOf(part)
                    if (index >= 0) {
                        remaining = remaining.substring(index + part.length)
                        true
                    } else {
                        false
                    }
                }
            }
            else -> text.contains(pattern)
        }
    }
}

/**
 * UI State for Network Logs screen
 */
data class NetworkLogsUiState(
    val logs: List<NetworkLogEntry> = emptyList(),
    val filteredLogs: List<NetworkLogEntry> = emptyList(),
    val isLoading: Boolean = true,
    val searchText: String = "",
    val selectedMethods: Set<String> = emptySet(),
    val selectedStatusRanges: Set<String> = emptySet(),
    val availableMethods: List<String> = emptyList(),
    val advancedFilter: NetworkFilterConfig = NetworkFilterConfig(),
) {
    val hasActiveFilters: Boolean
        get() =
            selectedMethods.isNotEmpty() ||
                selectedStatusRanges.isNotEmpty() ||
                advancedFilter.hasActiveFilters

    val totalActiveFilterCount: Int
        get() {
            var count = 0
            if (selectedMethods.isNotEmpty()) count++
            if (selectedStatusRanges.isNotEmpty()) count++
            count += advancedFilter.activeFilterCount
            return count
        }
}
