package com.spectra.logger.feature.network.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.network.statistics.NetworkDashboardStatistics
import com.spectra.logger.feature.network.statistics.NetworkFilterEngineRepositoryImpl
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * UI State for the Network Statistics/Dashboard screen.
 */
data class NetworkStatisticsUiState(
    val statistics: NetworkDashboardStatistics =
        NetworkDashboardStatistics(
            totalRequests = 0,
            statusCounts = persistentMapOf(),
            latencyDistribution = persistentMapOf(),
            timeline = persistentListOf(),
        ),
    val isLoading: Boolean = true,
)

/**
 * ViewModel that bridges the domain statistical aggregation engine to the Network Dashboard UI.
 */
class NetworkStatisticsViewModel(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    // Lazily initialize the repository using the central storage
    private val repository = NetworkFilterEngineRepositoryImpl(SpectraLogger.networkStorage, dispatcher)

    // Map the domain statistics directly to the UI state
    val uiState: StateFlow<NetworkStatisticsUiState> =
        repository.statistics
            .map { stats ->
                NetworkStatisticsUiState(
                    statistics = stats,
                    isLoading = false,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NetworkStatisticsUiState(isLoading = true),
            )

    init {
        repository.startObserving(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopObserving()
    }
}
