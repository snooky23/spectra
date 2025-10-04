package com.spectra.logger.ui.theme

import androidx.compose.ui.graphics.Color
import com.spectra.logger.domain.model.LogLevel

/**
 * Color scheme for log levels.
 */
object LogColors {
    val Verbose = Color(0xFF9E9E9E) // Gray
    val Debug = Color(0xFF2196F3) // Blue
    val Info = Color(0xFF4CAF50) // Green
    val Warning = Color(0xFFFFC107) // Amber/Yellow
    val Error = Color(0xFFF44336) // Red
    val Fatal = Color(0xFFB71C1C) // Dark Red

    /**
     * Get color for a specific log level.
     */
    fun getColor(level: LogLevel): Color =
        when (level) {
            LogLevel.VERBOSE -> Verbose
            LogLevel.DEBUG -> Debug
            LogLevel.INFO -> Info
            LogLevel.WARNING -> Warning
            LogLevel.ERROR -> Error
            LogLevel.FATAL -> Fatal
        }

    /**
     * Background colors for network status codes.
     */
    object NetworkStatus {
        val Success = Color(0xFF4CAF50) // Green (2xx)
        val Redirect = Color(0xFF2196F3) // Blue (3xx)
        val ClientError = Color(0xFFFFC107) // Yellow (4xx)
        val ServerError = Color(0xFFF44336) // Red (5xx)
        val Unknown = Color(0xFF9E9E9E) // Gray

        fun getColor(statusCode: Int?): Color =
            when (statusCode) {
                null -> Unknown
                in 200..299 -> Success
                in 300..399 -> Redirect
                in 400..499 -> ClientError
                in 500..599 -> ServerError
                else -> Unknown
            }
    }
}
