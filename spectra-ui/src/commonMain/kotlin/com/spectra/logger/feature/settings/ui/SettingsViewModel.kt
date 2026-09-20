package com.spectra.logger.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.Version
import com.spectra.logger.core.ui.util.PlatformUtils
import com.spectra.logger.feature.events.storage.EventLogStorage
import com.spectra.logger.feature.logs.export.ExportFormat
import com.spectra.logger.feature.logs.export.LogExporter
import com.spectra.logger.feature.logs.model.LogFilter
import com.spectra.logger.feature.logs.storage.LogStorage
import com.spectra.logger.feature.network.storage.NetworkLogStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 *
 * Adheres to Clean Architecture with constructor-injected storage dependencies.
 */
class SettingsViewModel(
    private val logStorage: LogStorage = SpectraLogger.logStorage,
    private val networkStorage: NetworkLogStorage = SpectraLogger.networkStorage,
    private val eventStorage: EventLogStorage = SpectraLogger.eventStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
        refreshConfigState()
        observeLogCounts()
    }

    private fun observeLogCounts() {
        viewModelScope.launch {
            logStorage.observe().collect {
                _uiState.update { state ->
                    state.copy(applicationLogCount = state.applicationLogCount + 1)
                }
            }
        }
        viewModelScope.launch {
            networkStorage.observe().collect {
                _uiState.update { state ->
                    state.copy(networkLogCount = state.networkLogCount + 1)
                }
            }
        }
        viewModelScope.launch {
            eventStorage.observe().collect {
                _uiState.update { state ->
                    state.copy(eventLogCount = state.eventLogCount + 1)
                }
            }
        }
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
                val eventCount = eventStorage.count()

                _uiState.update {
                    it.copy(
                        applicationLogCount = logCount,
                        networkLogCount = networkCount,
                        eventLogCount = eventCount,
                    )
                }
            } catch (_: Exception) {
                // Ignore query errors
            }
        }
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        _uiState.update { it.copy(appearanceMode = mode) }
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

    fun clearEvents() {
        viewModelScope.launch {
            eventStorage.clear()
            _uiState.update { it.copy(eventLogCount = 0) }
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
        val domainList =
            domains.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        val currentFeatures = SpectraLogger.configuration.enabledFeatures
        SpectraLogger.configure {
            features {
                enableNetworkLogging = currentFeatures.enableNetworkLogging
                enableCrashReporting = currentFeatures.enableCrashReporting
                enablePerformanceMetrics = currentFeatures.enablePerformanceMetrics
                networkIgnoredDomains = domainList
                networkIgnoredExtensions = currentFeatures.networkIgnoredExtensions
            }
        }
        refreshConfigState()
    }

    fun updateIgnoredTokens(tokens: String) {
        val tokenList =
            tokens.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        val currentFeatures = SpectraLogger.configuration.enabledFeatures
        SpectraLogger.configure {
            features {
                enableNetworkLogging = currentFeatures.enableNetworkLogging
                enableCrashReporting = currentFeatures.enableCrashReporting
                enablePerformanceMetrics = currentFeatures.enablePerformanceMetrics
                networkIgnoredDomains = currentFeatures.networkIgnoredDomains
                networkIgnoredTokens = tokenList
                networkIgnoredExtensions = currentFeatures.networkIgnoredExtensions
            }
        }
        refreshConfigState()
    }

    fun updateMaxBodySize(size: String) {
        val parsedSize = size.toIntOrNull() ?: SpectraLogger.configuration.performanceConfig.maxBodySize
        val currentPerf = SpectraLogger.configuration.performanceConfig
        SpectraLogger.configure {
            performance {
                flowBufferCapacity = currentPerf.flowBufferCapacity
                asyncWriteTimeout = currentPerf.asyncWriteTimeout
                maxBodySize = parsedSize
            }
        }
        refreshConfigState()
    }

    suspend fun getExportAllText(): String =
        LogExporter.exportFullBundleAsMarkdown(
            logStorage = logStorage,
            networkStorage = networkStorage,
            eventStorage = eventStorage,
        )

    fun exportAllLogs(
        format: ExportFormat = ExportFormat.MARKDOWN,
        context: Any? = null,
    ) {
        viewModelScope.launch {
            val text =
                if (format == ExportFormat.JSON) {
                    LogExporter.exportFullBundleAsJson(
                        logStorage = logStorage,
                        networkStorage = networkStorage,
                        eventStorage = eventStorage,
                    )
                } else {
                    LogExporter.exportFullBundleAsMarkdown(
                        logStorage = logStorage,
                        networkStorage = networkStorage,
                        eventStorage = eventStorage,
                    )
                }
            PlatformUtils.shareText(text, "Export All Telemetry", context)
        }
    }

    private fun refreshConfigState() {
        val config = SpectraLogger.configuration
        _uiState.update {
            it.copy(
                isNetworkLoggingEnabled = config.enabledFeatures.enableNetworkLogging,
                isFilePersistenceEnabled = config.logStorageConfig.enablePersistence,
                ignoredDomainsText = config.enabledFeatures.networkIgnoredDomains.joinToString(", "),
                ignoredTokensText = config.enabledFeatures.networkIgnoredTokens.joinToString(", "),
                maxBodySizeText = config.performanceConfig.maxBodySize.toString(),
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
    val eventLogCount: Int = 0,
    val version: String = Version.LIBRARY_VERSION,
    val isNetworkLoggingEnabled: Boolean = true,
    val isFilePersistenceEnabled: Boolean = false,
    val ignoredDomainsText: String = "",
    val ignoredTokensText: String = "",
    val maxBodySizeText: String = "250000",
)

enum class AppearanceMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System"),
}
