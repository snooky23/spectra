package com.spectra.logger.core.ui.compose

import androidx.compose.ui.window.ComposeUIViewController
import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
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
