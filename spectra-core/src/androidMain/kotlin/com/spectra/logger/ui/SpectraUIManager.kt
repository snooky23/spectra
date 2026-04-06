package com.spectra.logger.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of the Spectra Logger UI manager.
 */
actual object SpectraUIManager {
    private val _isShowing = MutableStateFlow(false)
    val isShowing: StateFlow<Boolean> = _isShowing.asStateFlow()

    actual fun showScreen() {
        _isShowing.value = true
    }

    actual fun dismissScreen() {
        _isShowing.value = false
    }
}
