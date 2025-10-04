package com.spectra.logger.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects shake gestures on Android devices.
 *
 * Usage:
 * ```kotlin
 * val detector = ShakeDetector(context) {
 *     // Shake detected - show logger UI
 *     SpectraLoggerUI.show(context)
 * }
 * detector.start()
 * ```
 */
class ShakeDetector(
    context: Context,
    private val onShakeDetected: () -> Unit,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime: Long = 0
    private var shakeCount = 0

    /**
     * Start listening for shake gestures.
     */
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI,
            )
        }
    }

    /**
     * Stop listening for shake gestures.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate acceleration magnitude (excluding gravity)
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH

        val now = System.currentTimeMillis()

        if (acceleration > SHAKE_THRESHOLD) {
            if (now - lastShakeTime < SHAKE_TIME_WINDOW) {
                shakeCount++
                if (shakeCount >= SHAKE_COUNT_THRESHOLD) {
                    onShakeDetected()
                    shakeCount = 0
                    lastShakeTime = 0
                }
            } else {
                shakeCount = 1
            }
            lastShakeTime = now
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) {
        // Not needed
    }

    companion object {
        private const val SHAKE_THRESHOLD = 12.0 // m/s²
        private const val SHAKE_TIME_WINDOW = 500L // ms
        private const val SHAKE_COUNT_THRESHOLD = 3 // Number of shakes required
    }
}
