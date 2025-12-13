package com.spectra.logger.domain.model

/**
 * Application context information that applies to all logs.
 * This data is captured once during initialization and used to enrich all log entries.
 *
 * Required fields:
 * @property sessionId Unique session identifier for grouping related logs
 *
 * Optional but recommended:
 * @property appVersion Application version (e.g., "1.0.0")
 * @property buildNumber Build number or code (e.g., "42")
 * @property deviceModel Device model (e.g., "iPhone14Pro")
 * @property osVersion Operating system version (e.g., "17.0")
 * @property osName Operating system name (e.g., "iOS", "Android")
 *
 * Optional:
 * @property userId Logged-in user ID for user-specific debugging
 * @property environment Deployment environment (e.g., "production", "staging", "debug")
 * @property customAttributes Additional app-specific context
 */
data class AppContext(
    val sessionId: String,
    val appVersion: String? = null,
    val buildNumber: String? = null,
    val deviceModel: String? = null,
    val osVersion: String? = null,
    val osName: String? = null,
    val userId: String? = null,
    val environment: String = "production",
    val customAttributes: Map<String, String> = emptyMap(),
) {
    /**
     * Returns a map representation of the app context for use in log metadata.
     * Only includes non-null values to keep metadata clean.
     * Useful for enriching log entries with environment information.
     */
    fun toMetadataMap(): Map<String, String> {
        val contextMap =
            mutableMapOf(
                "session_id" to sessionId,
                "environment" to environment,
            )

        appVersion?.let { contextMap["app_version"] = it }
        buildNumber?.let { contextMap["build_number"] = it }
        deviceModel?.let { contextMap["device_model"] = it }
        osVersion?.let { contextMap["os_version"] = it }
        osName?.let { contextMap["os_name"] = it }
        userId?.let { contextMap["user_id"] = it }

        contextMap.putAll(customAttributes)

        return contextMap
    }
}
