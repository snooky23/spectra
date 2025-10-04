package com.spectra.logger.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

/**
 * Helper for enabling automatic logger UI triggers.
 *
 * Usage in your Application class:
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         SpectraLoggerTrigger.enableShakeToOpen(this)
 *     }
 * }
 * ```
 */
object SpectraLoggerTrigger {
    private var shakeDetector: ShakeDetector? = null
    private var activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null

    /**
     * Enable shake gesture to open logger UI.
     * Automatically starts/stops detection based on activity lifecycle.
     *
     * @param application The application instance
     */
    fun enableShakeToOpen(application: Application) {
        if (activityLifecycleCallbacks != null) {
            // Already enabled
            return
        }

        val callbacks =
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    shakeDetector =
                        ShakeDetector(activity) {
                            SpectraLoggerUI.show(activity)
                        }
                    shakeDetector?.start()
                }

                override fun onActivityPaused(activity: Activity) {
                    shakeDetector?.stop()
                    shakeDetector = null
                }

                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}

                override fun onActivityDestroyed(activity: Activity) {}
            }

        activityLifecycleCallbacks = callbacks
        application.registerActivityLifecycleCallbacks(callbacks)
    }

    /**
     * Disable shake gesture detection.
     *
     * @param application The application instance
     */
    fun disableShakeToOpen(application: Application) {
        activityLifecycleCallbacks?.let {
            application.unregisterActivityLifecycleCallbacks(it)
        }
        activityLifecycleCallbacks = null
        shakeDetector?.stop()
        shakeDetector = null
    }

    /**
     * Show logger UI programmatically.
     *
     * @param context The context to use
     */
    fun showLogger(context: Context) {
        SpectraLoggerUI.show(context)
    }
}
