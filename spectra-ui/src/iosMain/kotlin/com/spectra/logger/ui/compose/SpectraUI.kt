package com.spectra.logger.ui.compose

import com.spectra.logger.ui.SpectraUIManager

/**
 * Initialization point for Spectra UI on iOS.
 */
object SpectraUI {
    /**
     * Initializes the UI module and registers the view controller provider with the core module.
     */
    fun init() {
        SpectraUIManager.registerControllerProvider {
            SpectraLoggerViewController(onDismiss = { SpectraUIManager.dismissScreen() })
        }
    }
}
