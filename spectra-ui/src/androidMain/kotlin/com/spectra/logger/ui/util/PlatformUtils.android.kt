package com.spectra.logger.ui.util

import android.content.Context
import android.content.Intent

/**
 * Android implementation of PlatformUtils.
 */
actual object PlatformUtils {
    /**
     * Share text using the Android Intent system.
     *
     * @param text The text to share.
     * @param title The title of the share sheet.
     * @param context Must be an android.content.Context instance.
     */
    actual fun shareText(
        text: String,
        title: String,
        context: Any?,
    ) {
        val androidContext = context as? Context ?: return
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
        androidContext.startActivity(Intent.createChooser(intent, title))
    }
}
