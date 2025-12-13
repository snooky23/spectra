package com.spectra.logger.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.Version
import com.spectra.logger.domain.storage.LogFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen
 */
class SettingsViewModel : ViewModel() {
    
    private val logStorage = SpectraLogger.logStorage
    private val networkStorage = SpectraLogger.networkLogStorage
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val noFilter = LogFilter(
                    levels = null,
                    tags = null,
                    searchText = null,
                    fromTimestamp = null,
                    toTimestamp = null
                )
                val logCount = logStorage.query(filter = noFilter, limit = null).size
                val networkCount = networkStorage.queryAll().size
                
                _uiState.update {
                    it.copy(
                        applicationLogCount = logCount,
                        networkLogCount = networkCount
                    )
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        _uiState.update { it.copy(appearanceMode = mode) }
        // TODO: Persist preference
    }

    fun clearApplicationLogs() {
        viewModelScope.launch {
            logStorage.clear()
            _uiState.update { it.copy(applicationLogCount = 0) }
        }
    }

    fun clearNetworkLogs() {
        viewModelScope.launch {
            networkStorage.clear()
            _uiState.update { it.copy(networkLogCount = 0) }
        }
    }
}

/**
 * UI State for Settings screen
 */
data class SettingsUiState(
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val applicationLogCount: Int = 0,
    val networkLogCount: Int = 0,
    val version: String = Version.LIBRARY_VERSION
)
