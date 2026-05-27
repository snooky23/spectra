package com.spectra.logger.core.ui

/**
 * Main UI entry point for the Spectra Logger framework.
 *
 * **UI Display:**
 * ```
 * SpectraUI.showScreen()   // Show logger UI as modal
 * SpectraUI.dismissScreen() // Dismiss logger UI
 * ```
 *
 * @since 0.0.1
 */
object SpectraUI {
    /**
     * Show the Spectra Logger debug UI as a modal screen.
     *
     * This displays the logger interface above the running app without interrupting
     * the app's lifecycle. Users can dismiss the UI at any time.
     *
     * The UI includes:
     * - Log viewer with real-time updates
     * - Network request/response viewer
     * - Settings and configuration options
     * - Export and share functionality
     *
     * **Platform-specific behavior:**
     * - **Android**: Shows as a fullscreen modal dialog
     * - **iOS**: Shows as a modal presentation over the current view
     *
     * @see dismissScreen
     */
    fun showScreen() = SpectraUIManager.showScreen()

    /**
     * Dismiss the Spectra Logger debug UI.
     *
     * Closes the modal screen if it is currently shown.
     * Safe to call even if the screen is not currently visible.
     *
     * @see showScreen
     */
    fun dismissScreen() = SpectraUIManager.dismissScreen()
}
