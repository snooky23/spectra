package com.spectra.logger.feature.crash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.crash.model.Breadcrumb
import com.spectra.logger.feature.crash.model.BreadcrumbType
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import com.spectra.logger.feature.crash.storage.CrashStorage
import com.spectra.logger.feature.logs.export.ExportFormat
import com.spectra.logger.feature.logs.export.LogExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CrashUiState(
    val crashes: List<CrashReport> = emptyList(),
    val selectedCrash: CrashReport? = null,
    val isLoading: Boolean = false,
)

class CrashViewModel(
    private val crashStorage: CrashStorage = SpectraLogger.crashStorage,
    externalScope: CoroutineScope? = null,
) : ViewModel() {
    private val scope = externalScope ?: viewModelScope

    private val _uiState = MutableStateFlow(CrashUiState(isLoading = true))
    val uiState: StateFlow<CrashUiState> = _uiState.asStateFlow()

    init {
        observeCrashes()
    }

    private fun observeCrashes() {
        scope.launch {
            crashStorage.observeCrashes().collect { crashes ->
                _uiState.update { current ->
                    current.copy(
                        crashes = crashes,
                        isLoading = false,
                        selectedCrash =
                            if (current.selectedCrash != null && crashes.none { it.id == current.selectedCrash.id }) {
                                null
                            } else {
                                current.selectedCrash
                            },
                    )
                }
            }
        }
    }

    fun selectCrash(crash: CrashReport?) {
        _uiState.update { it.copy(selectedCrash = crash) }
    }

    fun clearCrashes() {
        scope.launch {
            crashStorage.clear()
            _uiState.update { it.copy(crashes = emptyList(), selectedCrash = null) }
        }
    }

    fun simulateTestCrash() {
        scope.launch {
            val testCrash =
                CrashReport(
                    id = IdGenerator.generate(),
                    timestamp = SpectraTime.now().toEpochMilliseconds(),
                    exceptionClass = "java.lang.RuntimeException",
                    message = "Simulated test crash triggered from Spectra Debug UI",
                    stackTrace =
                        "java.lang.RuntimeException: Simulated test crash\n" +
                            "\tat com.spectra.logger.feature.crash.ui.CrashViewModel.simulateTestCrash(CrashViewModel.kt:65)",
                    threadName = "main",
                    severity = CrashSeverity.NON_FATAL,
                    breadcrumbs =
                        listOf(
                            Breadcrumb(
                                timestamp = SpectraTime.now().toEpochMilliseconds() - 500,
                                type = BreadcrumbType.USER_ACTION,
                                category = "DebugUI",
                                message = "Clicked 'Simulate Test Crash' button",
                            ),
                        ),
                    metadata = mapOf("simulated" to "true", "environment" to "debug"),
                )
            crashStorage.recordCrash(testCrash)
        }
    }

    fun exportCrash(
        crash: CrashReport,
        format: ExportFormat,
    ): String =
        when (format) {
            ExportFormat.TEXT -> LogExporter.exportCrashAsText(crash)
            ExportFormat.JSON -> LogExporter.exportCrashAsJson(crash)
            ExportFormat.MARKDOWN -> LogExporter.exportCrashAsMarkdown(crash)
            else -> LogExporter.exportCrashAsMarkdown(crash)
        }
}
