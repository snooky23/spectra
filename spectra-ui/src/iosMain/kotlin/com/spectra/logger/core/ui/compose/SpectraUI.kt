package com.spectra.logger.core.ui.compose

import com.spectra.logger.core.model.*
import com.spectra.logger.core.ui.SpectraUIManager
import com.spectra.logger.core.utils.*

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
