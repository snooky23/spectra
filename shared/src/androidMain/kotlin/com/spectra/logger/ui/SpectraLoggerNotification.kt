package com.spectra.logger.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Helper for showing a persistent notification that opens the logger UI.
 *
 * Usage:
 * ```kotlin
 * // Show persistent notification
 * SpectraLoggerNotification.show(context, "Debug Mode Active")
 *
 * // Hide notification
 * SpectraLoggerNotification.hide(context)
 * ```
 */
object SpectraLoggerNotification {
    private const val CHANNEL_ID = "spectra_logger"
    private const val NOTIFICATION_ID = 7890

    /**
     * Show a persistent notification that opens the logger when tapped.
     *
     * @param context The context
     * @param title The notification title
     * @param message Optional notification message
     */
    fun show(
        context: Context,
        title: String = "Spectra Logger",
        message: String = "Tap to view logs",
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required for Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Spectra Logger",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Development logger notifications"
                    setShowBadge(false)
                }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open logger
        val intent = Intent(context, SpectraLoggerActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        // Build notification
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Hide the logger notification.
     *
     * @param context The context
     */
    fun hide(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
