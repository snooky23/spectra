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

    fun toggleMethod(method: String) {
        _uiState.update { state ->
            val newMethods = if (state.selectedMethods.contains(method)) {
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
            val newRanges = if (state.selectedStatusRanges.contains(range)) {
                state.selectedStatusRanges - range
            } else {
                state.selectedStatusRanges + range
            }
            state.copy(selectedStatusRanges = newRanges)
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

            // Filter by method
            if (state.selectedMethods.isNotEmpty()) {
                filtered = filtered.filter { state.selectedMethods.contains(it.method) }
            }

            // Filter by status code range
            if (state.selectedStatusRanges.isNotEmpty()) {
                filtered = filtered.filter { log ->
                    val status = log.responseCode ?: return@filter false
                    state.selectedStatusRanges.any { range ->
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
                filtered = filtered.filter { log ->
                    log.url.lowercase().contains(query) ||
                    log.method.lowercase().contains(query)
                }
            }

            state.copy(filteredLogs = filtered)
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
    val availableMethods: List<String> = emptyList()
)
