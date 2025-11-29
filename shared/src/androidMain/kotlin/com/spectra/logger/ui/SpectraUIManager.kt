package com.spectra.logger.ui

/**
 * Android implementation of the Spectra Logger UI manager.
 *
 * Manages showing and dismissing the logger UI as a modal dialog.
 *
 * Implementation details:
 * - Shows as a fullscreen modal dialog from the current context
 * - Preserves app lifecycle (doesn't affect Activity/Fragment state)
 * - Safe to call repeatedly (idempotent)
 *
 * @since 0.0.1
 */
actual object SpectraUIManager {
    private var currentDialog: android.app.Dialog? = null

    /**
     * Show the Spectra Logger debug UI as a modal dialog.
     *
     * Shows a fullscreen modal dialog above the current activity.
     * Safe to call multiple times (subsequent calls are ignored if already shown).
     */
    actual fun showScreen() {
        // TODO: Implement Android UI modal presentation
        // This will create and show the logger UI as a fullscreen dialog
    }

    /**
     * Dismiss the Spectra Logger debug UI.
     *
     * Closes the modal dialog if shown.
     * Safe to call even if not currently shown.
     */
    actual fun dismissScreen() {
        // TODO: Implement Android UI dismissal
        currentDialog?.dismiss()
        currentDialog = null
    }
}
