package com.spectra.logger.utils

import com.spectra.logger.domain.model.SourceType
import platform.Foundation.NSThread

/**
 * iOS implementation of SourceDetector.
 * Uses NSThread.callStackSymbols to get the call stack.
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
     * iOS-specific implementation using NSThread API.
     */
    private fun getStackTrace(): List<String> {
        return try {
            NSThread.callStackSymbols.map { it as String }
        } catch (e: Exception) {
            // Fallback if stack trace not available
            emptyList()
        }
    }

    /**
     * Extracts the source package ID from the stack trace.
     *
     * For iOS, the stack symbols contain memory addresses and framework names.
     * We extract the framework/module name from the symbol.
     */
    private fun extractSourceIdFromStack(stackTrace: List<String>): String {
        if (stackTrace.isEmpty()) {
            return "unknown"
        }

        // Skip internal logger packages
        val internalPackages =
            setOf(
                "Spectra",
                "Swift",
                "Foundation",
                "CoreFoundation",
                "UIKit",
                "SwiftUI",
                "libsystem",
            )

        // Extract framework name from stack symbols
        // Format: FrameworkName + 0x... (address)
        for (symbol in stackTrace) {
            val parts = symbol.trim().split(" ")
            if (parts.isNotEmpty()) {
                val frameworkName = parts[0]
                if (frameworkName.isNotEmpty() &&
                    !internalPackages.any {
                        frameworkName.contains(
                            it,
                            ignoreCase = true,
                        )
                    }
                ) {
                    return frameworkName.lowercase()
                }
            }
        }

        return "unknown"
    }

    /**
     * Checks if a framework name is a known SDK.
     *
     * This is a fallback for common SDKs that might not have obvious identifiers.
     */
    private fun isKnownSdk(sourceId: String): Boolean {
        val knownSdks =
            setOf(
                "firebase",
                "google",
                "facebook",
                "sentry",
                "amplitude",
                "mixpanel",
                "segment",
                "crashlytics",
                "analytics",
            )

        return knownSdks.any { sourceId.contains(it, ignoreCase = true) }
    }
}
