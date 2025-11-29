package com.spectra.logger.ui

/**
 * Platform-specific manager for showing and dismissing the Spectra Logger UI.
 *
 * Implementation provided by expect/actual pattern on Android and iOS.
 *
 * @since 0.0.1
 */
expect object SpectraUIManager {
    /**
     * Show the Spectra Logger debug UI as a modal screen.
     */
    fun showScreen()

    /**
     * Dismiss the Spectra Logger debug UI.
     */
    fun dismissScreen()
}
