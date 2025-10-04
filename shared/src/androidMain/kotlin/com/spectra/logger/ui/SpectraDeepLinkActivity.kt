package com.spectra.logger.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/**
 * Activity that handles deep links to open the logger.
 *
 * Supports URL schemes like:
 * - spectra://open - Open main logger screen
 * - spectra://logs - Open logs tab
 * - spectra://network - Open network tab
 * - spectra://logs?level=ERROR - Open logs filtered by level
 *
 * Usage from ADB:
 * ```
 * adb shell am start -a android.intent.action.VIEW -d "spectra://open"
 * ```
 *
 * Usage from browser/webview:
 * ```
 * window.location = "spectra://open"
 * ```
 */
class SpectraDeepLinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Parse the deep link
        val uri: Uri? = intent?.data

        if (uri != null && uri.scheme == "spectra") {
            handleDeepLink(uri)
        }

        // Close this transparent activity
        finish()
    }

    private fun handleDeepLink(uri: Uri) {
        // Launch the logger activity
        val intent =
            Intent(this, SpectraLoggerActivity::class.java).apply {
                // Pass the deep link data to the logger activity
                putExtra(EXTRA_DEEP_LINK_HOST, uri.host)
                putExtra(EXTRA_DEEP_LINK_QUERY, uri.query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        startActivity(intent)
    }

    companion object {
        const val EXTRA_DEEP_LINK_HOST = "deep_link_host"
        const val EXTRA_DEEP_LINK_QUERY = "deep_link_query"
    }
}
