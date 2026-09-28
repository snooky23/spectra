package com.spectra.logger.core.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * JVM / Desktop implementation of SpectraUIManager.
 */
actual object SpectraUIManager {
    var isVisible by mutableStateOf(false)
        private set

    /**
     * Show the Spectra Logger debug UI.
     */
    actual fun showScreen() {
        isVisible = true
    }

    /**
     * Dismiss the Spectra Logger debug UI.
     */
    actual fun dismissScreen() {
        isVisible = false
    }
}
