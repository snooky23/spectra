package com.spectra.logger.feature.logs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.logs.statistics.DashboardStatistics
import com.spectra.logger.feature.logs.statistics.FilterEngineRepository
import com.spectra.logger.feature.logs.statistics.FilterEngineRepositoryImpl
import com.spectra.logger.feature.logs.storage.LogStorage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
 *
 * Implements Clean Architecture with Dependency Inversion via constructor injection.
 */
class StatisticsViewModel(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    storage: LogStorage = SpectraLogger.logStorage,
) : ViewModel() {
    private val repository: FilterEngineRepository = FilterEngineRepositoryImpl(storage, dispatcher)

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

    fun onTimeRangeSelected(
        startIndex: Int,
        endIndex: Int,
    ) {
        // TODO: In Epic 4, this will update the global filter state with the selected time window.
    }

    fun onLevelSliceTapped(level: LogLevel) {
        // TODO: In Epic 4, this will generate a log-level filter chip.
    }
}
