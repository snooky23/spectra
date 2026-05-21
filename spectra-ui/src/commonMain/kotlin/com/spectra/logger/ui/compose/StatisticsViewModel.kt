package com.spectra.logger.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.statistics.DashboardStatistics
import com.spectra.logger.domain.statistics.FilterEngineRepositoryImpl
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * UI State for the Statistics/Dashboard screen.
 */
data class StatisticsUiState(
    val statistics: DashboardStatistics =
        DashboardStatistics(
            timeline = persistentListOf(),
            levelCounts = persistentMapOf(),
            tagCounts = persistentMapOf(),
            totalLogs = 0,
        ),
    val isLoading: Boolean = true,
)

/**
 * ViewModel that bridges the domain statistical aggregation engine to the UI.
 */
class StatisticsViewModel : ViewModel() {
    // Lazily initialize the repository using the central storage
    private val repository = FilterEngineRepositoryImpl(SpectraLogger.logStorage)

    // Map the domain statistics directly to the UI state
    val uiState: StateFlow<StatisticsUiState> =
        repository.statistics
            .map { stats ->
                StatisticsUiState(
                    statistics = stats,
                    // Since FilterEngineRepository computes instantly, we mark as loaded when stats emit
                    isLoading = false,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = StatisticsUiState(isLoading = true),
            )

    init {
        repository.startObserving(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopObserving()
    }

    // --- Intent Functions (to be wired to UI interactions) ---

    fun onTimelineBucketTapped(timestamp: Long) {
        // TODO: In Epic 4, this will generate a time-range filter chip.
    }

    fun onTimeRangeSelected(startIndex: Int, endIndex: Int) {
        // TODO: In Epic 4, this will update the global filter state with the selected time window.
    }

    fun onLevelSliceTapped(level: LogLevel) {
        // TODO: In Epic 4, this will generate a log-level filter chip.
    }
}
