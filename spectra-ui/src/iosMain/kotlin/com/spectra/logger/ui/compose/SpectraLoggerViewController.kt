package com.spectra.logger.ui.compose

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
