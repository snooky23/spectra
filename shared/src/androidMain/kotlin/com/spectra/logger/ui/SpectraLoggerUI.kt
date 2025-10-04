package com.spectra.logger.ui

import android.content.Context
import android.content.Intent

/**
 * Helper object for launching Spectra Logger UI on Android.
 */
object SpectraLoggerUI {
    /**
     * Launch the Spectra Logger Activity.
     *
     * Usage:
     * ```kotlin
     * SpectraLoggerUI.show(context)
     * ```
     *
     * @param context Android context
     */
    fun show(context: Context) {
        val intent = Intent(context, SpectraLoggerActivity::class.java)
        context.startActivity(intent)
    }
}
