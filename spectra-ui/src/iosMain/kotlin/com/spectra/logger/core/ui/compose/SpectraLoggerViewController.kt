package com.spectra.logger.core.ui.compose

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Creates a UIViewController that hosts the Spectra Logger UI.
 * This is used by iOS applications to present the logger.
 */
fun SpectraLoggerViewController(onDismiss: () -> Unit): UIViewController {
    return ComposeUIViewController(configure = {
        enforceStrictPlistSanityCheck = false
    }) {
        SpectraLoggerScreen(onDismiss = onDismiss)
    }
}
