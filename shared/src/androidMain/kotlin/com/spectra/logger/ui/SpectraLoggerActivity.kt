package com.spectra.logger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.spectra.logger.ui.screens.SpectraLoggerScreen

/**
 * Android Activity for displaying the Spectra Logger UI.
 *
 * Usage:
 * ```kotlin
 * // In your app code:
 * startActivity(Intent(this, SpectraLoggerActivity::class.java))
 *
 * // Or via URL scheme:
 * // spectra://open - Opens main screen
 * // spectra://logs - Opens logs tab
 * // spectra://network - Opens network tab
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
