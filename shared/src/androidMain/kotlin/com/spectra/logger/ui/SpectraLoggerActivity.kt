package com.spectra.logger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.spectra.logger.ui.screens.SpectraLoggerScreen

/**
 * Android Activity for displaying the Spectra Logger UI.
 *
 * Usage:
 * ```kotlin
 * // In your app code:
 * startActivity(Intent(this, SpectraLoggerActivity::class.java))
 * ```
 */
class SpectraLoggerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SpectraLoggerScreen()
            }
        }
    }
}
