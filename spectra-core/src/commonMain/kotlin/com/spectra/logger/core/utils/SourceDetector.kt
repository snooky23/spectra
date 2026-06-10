package com.spectra.logger.core.utils

import com.spectra.logger.core.model.*
import com.spectra.logger.core.model.SourceType

/**
 * Detects the source (origin) of a log or network request automatically
 * by analyzing the call stack.
 *
 * This allows differentiation between logs from the main app vs SDKs
 * without requiring explicit configuration by developers.
 */
object SourceDetector {
    /**
     * Detects the source package/bundle ID from the call stack.
     *
     * Analyzes stack frames to identify which package/library made the call.
     *
     * @return A pair of (sourceId, sourceType) where sourceId is the package name
     * and sourceType indicates whether it's APP, SDK, or PLUGIN
     */
    fun detectSource(): Pair<String, SourceType> {
        val stackTrace = getStackTrace()
        val sourceId = extractSourceIdFromStack(stackTrace)

        val sourceType =
            when {
                sourceId.contains("spectra") -> SourceType.SDK
                isKnownSdk(sourceId) -> SourceType.SDK
                sourceId.contains("plugin") -> SourceType.PLUGIN
                else -> SourceType.APP
            }

        return sourceId to sourceType
    }

    /**
     * Gets the current call stack as a list of stack frame information.
     * Common implementation using Exception stack trace.
     */
    private fun getStackTrace(): List<String> {
        return try {
            Exception().stackTraceToString().split('\n')
        } catch (e: Exception) {
            // Fallback if stack trace not available
            emptyList()
        }
    }

    /**
     * Extracts the source package ID from the stack trace.
     *
     * Skips internal logging framework packages and returns the first
     * external package found in the call stack.
     */
    internal fun extractSourceIdFromStack(stackTrace: List<String>): String {
        if (stackTrace.isEmpty()) {
            return "unknown"
        }

        // Skip internal logger packages
        val internalPackages =
            setOf(
                "com.spectra.logger",
                "Spectra",
                "kotlin.coroutines",
                "kotlinx.coroutines",
                "java.lang",
                "android.util",
                "Swift",
                "Foundation",
                "CoreFoundation",
                "UIKit",
                "SwiftUI",
                "libsystem",
                "detectSource",
                "logNetwork",
            )

        // Find the first non-internal package
        for (frame in stackTrace) {
            // Basic parsing to extract possible package/framework name
            val trimmed = frame.trim()
            val potentialName =
                when {
                    trimmed.startsWith("at ") -> { // JVM style or Web style
                        if (trimmed.contains("http://") || trimmed.contains("https://")) {
                            // Web style: "at functionName (http://...)"
                            trimmed.removePrefix("at ").substringBefore('(').trim()
                        } else {
                            val fullMethod = trimmed.removePrefix("at ").substringBefore('(')
                            fullMethod.substringBeforeLast('.')
                        }
                    }
                    trimmed.contains("@http://") || trimmed.contains("@https://") -> { // JS Safari/Firefox style
                        trimmed.substringBefore('@').trim()
                    }
                    else -> { // iOS/Native style or unformatted
                        val parts = trimmed.split(Regex("\\s+"))
                        if (parts.size >= 2) {
                            if (parts[0].all { it.isDigit() }) parts[1] else parts[0]
                        } else {
                            ""
                        }
                    }
                }

            if (potentialName.isNotEmpty() && !internalPackages.any { potentialName.startsWith(it, ignoreCase = true) }) {
                return potentialName.lowercase()
            }
        }

        return "unknown"
    }

    /**
     * Checks if a package ID or framework name is a known SDK.
     *
     * This is a fallback for common SDKs that might not have obvious identifiers.
     */
    private fun isKnownSdk(sourceId: String): Boolean {
        val knownSdks =
            setOf(
                "com.google", "google",
                "com.facebook", "facebook",
                "com.firebase", "firebase",
                "com.crashlytics", "crashlytics",
                "io.sentry", "sentry",
                "com.amplitude", "amplitude",
                "com.mixpanel", "mixpanel",
                "com.segment", "segment",
                "retrofit",
                "okhttp3",
                "androidx",
                "analytics",
            )

        return knownSdks.any { sourceId.contains(it, ignoreCase = true) }
    }
}
