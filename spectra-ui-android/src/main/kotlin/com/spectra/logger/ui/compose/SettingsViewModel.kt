package com.spectra.logger.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.Version
import com.spectra.logger.domain.model.LogFilter
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
    private val networkStorage = SpectraLogger.networkStorage

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
        refreshConfigState()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val noFilter =
                    LogFilter(
                        levels = null,
                        tags = null,
                        searchText = null,
                        fromTimestamp = null,
                        toTimestamp = null,
                    )
                val logCount = logStorage.query(filter = noFilter, limit = null).size
                val networkCount = networkStorage.count()

                _uiState.update {
                    it.copy(
                        applicationLogCount = logCount,
                        networkLogCount = networkCount,
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

    // --- Configuration Mutators --- //

    fun toggleNetworkLogging(enabled: Boolean) {
        val currentFeatures = SpectraLogger.configuration.enabledFeatures
        SpectraLogger.configure {
            features {
                enableNetworkLogging = enabled
                enableCrashReporting = currentFeatures.enableCrashReporting
                enablePerformanceMetrics = currentFeatures.enablePerformanceMetrics
                networkIgnoredDomains = currentFeatures.networkIgnoredDomains
                networkIgnoredExtensions = currentFeatures.networkIgnoredExtensions
            }
        }
        refreshConfigState()
    }

    fun toggleFilePersistence(enabled: Boolean) {
        val currentStorage = SpectraLogger.configuration.logStorageConfig
        SpectraLogger.configure {
            logStorage {
                maxCapacity = currentStorage.maxCapacity
                enablePersistence = enabled
                fileLogLevel = currentStorage.fileLogLevel
            }
        }
        refreshConfigState()
    }

    fun updateIgnoredDomains(domains: String) {
        val list = domains.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val currentFeatures = SpectraLogger.configuration.enabledFeatures
        SpectraLogger.configure {
            features {
                enableNetworkLogging = currentFeatures.enableNetworkLogging
                enableCrashReporting = currentFeatures.enableCrashReporting
                enablePerformanceMetrics = currentFeatures.enablePerformanceMetrics
                networkIgnoredDomains = list
                networkIgnoredExtensions = currentFeatures.networkIgnoredExtensions
            }
        }
        refreshConfigState()
    }

    private fun refreshConfigState() {
        val config = SpectraLogger.configuration
        _uiState.update {
            it.copy(
                isNetworkLoggingEnabled = config.enabledFeatures.enableNetworkLogging,
                isFilePersistenceEnabled = config.logStorageConfig.enablePersistence,
                ignoredDomainsText = config.enabledFeatures.networkIgnoredDomains.joinToString(", ")
            )
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
    val version: String = Version.LIBRARY_VERSION,
    val isNetworkLoggingEnabled: Boolean = true,
    val isFilePersistenceEnabled: Boolean = false,
    val ignoredDomainsText: String = "",
)
