package com.spectra.logger.example

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.spectra.logger.SpectraLogger

/**
 * Example activity demonstrating Spectra Logger usage.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView =
            TextView(this).apply {
                text = "Spectra Logger Example\nVersion: ${SpectraLogger.getVersion()}"
                textSize = TEXT_SIZE_SP
                setPadding(PADDING_DP, PADDING_DP, PADDING_DP, PADDING_DP)
            }

        setContentView(textView)
    }

    private companion object {
        private const val TEXT_SIZE_SP = 20f
        private const val PADDING_DP = 40
    }
}
