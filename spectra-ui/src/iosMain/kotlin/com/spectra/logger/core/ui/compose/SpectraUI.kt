package com.spectra.logger.core.ui.compose

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import com.spectra.logger.core.ui.SpectraUIManager

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
