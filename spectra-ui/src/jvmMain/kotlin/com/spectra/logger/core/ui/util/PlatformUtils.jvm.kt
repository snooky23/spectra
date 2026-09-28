package com.spectra.logger.core.ui.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * JVM / Desktop implementation of PlatformUtils.
 */
actual object PlatformUtils {
    /**
     * Share text by copying it to the system clipboard on desktop platforms.
     */
    actual fun shareText(
        text: String,
        title: String,
        context: Any?,
    ) {
        try {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        } catch (_: Exception) {
            // Ignored if headless or clipboard is inaccessible
        }
    }
}
