package com.spectra.logger.example

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.spectra.logger.SpectraLogger

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Spectra Logger Example\nVersion: ${SpectraLogger.getVersion()}"
            textSize = 20f
            setPadding(40, 40, 40, 40)
        }

        setContentView(textView)
    }
}
