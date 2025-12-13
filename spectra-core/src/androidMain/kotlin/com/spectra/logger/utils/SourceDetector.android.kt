package com.spectra.logger.utils

import com.spectra.logger.domain.model.SourceType

/**
 * Android implementation of SourceDetector.
 * Uses Thread.currentThread().stackTrace to get the call stack.
 */
actual object SourceDetector {
    /**
     * Detects the source package/bundle ID from the call stack.
     *
     * Analyzes stack frames to identify which package/library made the call.
     *
     * @return A pair of (sourceId, sourceType) where sourceId is the package name
     * and sourceType indicates whether it's APP, SDK, or PLUGIN
     */
    actual fun detectSource(): Pair<String, SourceType> {
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
     * Android-specific implementation using Thread API.
     */
    private fun getStackTrace(): List<String> {
        return try {
            Thread.currentThread().stackTrace.map { it.className }.toList()
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
    private fun extractSourceIdFromStack(stackTrace: List<String>): String {
        if (stackTrace.isEmpty()) {
            return "unknown"
        }

        // Skip internal logger packages
        val internalPackages =
            setOf(
                "com.spectra.logger",
                "kotlin.coroutines",
                "kotlinx.coroutines",
                "java.lang",
                "android.util",
            )

        // Find the first non-internal package
        for (frame in stackTrace) {
            val packageName = frame.substringBeforeLast('.')
            if (packageName.isNotEmpty() && !internalPackages.any { packageName.startsWith(it) }) {
                return packageName
            }
        }

        return "unknown"
    }

    /**
     * Checks if a package ID is a known SDK.
     *
     * This is a fallback for common SDKs that might not have obvious identifiers.
     */
    private fun isKnownSdk(sourceId: String): Boolean {
        val knownSdks =
            setOf(
                "com.google",
                "com.facebook",
                "com.firebase",
                "com.crashlytics",
                "io.sentry",
                "com.amplitude",
                "com.mixpanel",
                "com.segment",
                "retrofit",
                "okhttp3",
                "androidx",
            )

        return knownSdks.any { sourceId.startsWith(it) }
    }
}
