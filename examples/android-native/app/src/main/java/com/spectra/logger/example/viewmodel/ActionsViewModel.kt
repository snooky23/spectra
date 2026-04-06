package com.spectra.logger.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActionsViewModel : ViewModel() {
    private var counter = 0
    var isBackgroundLogging by mutableStateOf(false)
        private set
    
    private var backgroundLoggingJob: Job? = null

    fun logButtonTapped() {
        counter++
        SpectraLogger.i("Example", "Button tapped $counter times")
    }

    fun generateWarning() {
        SpectraLogger.w("Example", "Warning log generated")
    }

    fun generateError() {
        SpectraLogger.e("Example", "Error log generated")
    }

    fun generateErrorWithStackTrace() {
        val stackTrace = """
            Fatal error: Attempted to divide by zero
            Stack trace:
            0 SpectraExample                    0x0000000104b8e3a0 calculateDivision(_:) + 52
            1 SpectraExample                    0x0000000104b8e2c8 processUserInput(_:) + 120
            2 SpectraExample                    0x0000000104b8e1f0 handleButtonTap() + 88
        """.trimIndent()
        
        SpectraLogger.e(
            tag = "Example",
            message = "Fatal error: Attempted to divide by zero",
            metadata = mapOf(
                "operation" to "calculateDivision",
                "dividend" to "10",
                "divisor" to "0",
                "severity" to "CRITICAL",
                "error_type" to "ArithmeticException",
                "stack_trace" to stackTrace
            )
        )
    }

    fun generate10Logs() {
        val tags = listOf("Auth", "Network", "UI", "Database")
        viewModelScope.launch {
            for (i in 1..10) {
                val tag = tags[i % tags.count()]
                when (i % 4) {
                    0 -> SpectraLogger.d(tag, "Debug log entry #$i")
                    1 -> SpectraLogger.i(tag, "Info log entry #$i")
                    2 -> SpectraLogger.w(tag, "Warning log entry #$i")
                    else -> SpectraLogger.e(tag, "Error log entry #$i")
                }
            }
        }
    }

    fun generate100Logs() {
        val tags = listOf("Auth", "Network", "UI", "Database", "Cache", "API")
        viewModelScope.launch {
            for (i in 1..100) {
                val tag = tags[i % tags.count()]
                SpectraLogger.i(tag, "Batch log entry #$i - stress test")
            }
        }
    }

    fun toggleBackgroundLogging() {
        isBackgroundLogging = !isBackgroundLogging
        if (isBackgroundLogging) {
            startBackgroundLogging()
        } else {
            backgroundLoggingJob?.cancel()
        }
    }

    private fun startBackgroundLogging() {
        backgroundLoggingJob?.cancel()
        backgroundLoggingJob = viewModelScope.launch {
            var count = 0
            while (isBackgroundLogging) {
                count++
                SpectraLogger.i("BackgroundTask", "Real-time log #$count")
                delay(2000)
            }
        }
    }

    fun generateAllLogLevels() {
        SpectraLogger.v("LevelDemo", "This is a VERBOSE message")
        SpectraLogger.d("LevelDemo", "This is a DEBUG message")
        SpectraLogger.i("LevelDemo", "This is an INFO message")
        SpectraLogger.w("LevelDemo", "This is a WARNING message")
        SpectraLogger.e("LevelDemo", "This is an ERROR message")
        SpectraLogger.f("LevelDemo", "This is a FATAL message")
    }

    fun generateSearchableLogs() {
        SpectraLogger.i("SearchDemo", "Order #12345 placed successfully")
        SpectraLogger.i("SearchDemo", "User john@example.com logged in")
        SpectraLogger.w("SearchDemo", "Payment processing delayed for order #12345")
        SpectraLogger.e("SearchDemo", "Failed to send email to john@example.com")
    }
}
