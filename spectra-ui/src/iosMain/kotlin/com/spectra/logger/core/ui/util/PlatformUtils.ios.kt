package com.spectra.logger.core.ui.util

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/**
 * iOS implementation of PlatformUtils.
 */
actual object PlatformUtils {
    /**
     * Share text using UIActivityViewController.
     */
    actual fun shareText(
        text: String,
        title: String,
        context: Any?,
    ) {
        val viewController =
            context as? UIViewController
                ?: UIApplication.sharedApplication.keyWindow?.rootViewController
                ?: return

        val activityViewController =
            UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null,
            )

        viewController.presentViewController(activityViewController, animated = true, completion = null)
    }
}
