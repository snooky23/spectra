package com.spectra.logger.feature.streaming.model

import kotlinx.serialization.Serializable

/**
 * Metadata identifying the device running Spectra Logger.
 */
@Serializable
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val os: String,
    val osVersion: String,
    val appVersion: String,
)

/**
 * Interface providing device metadata for remote streaming handshakes.
 */
interface DeviceInfoProvider {
    fun getDeviceInfo(): DeviceInfo
}

/**
 * Default multiplatform implementation of [DeviceInfoProvider].
 */
open class SimpleDeviceInfoProvider(
    private val deviceId: String = "spectra-device",
    private val deviceName: String = "Spectra Client",
    private val os: String = "Multiplatform",
    private val osVersion: String = "1.0",
    private val appVersion: String = "1.0.0",
) : DeviceInfoProvider {
    override fun getDeviceInfo(): DeviceInfo =
        DeviceInfo(
            deviceId = deviceId,
            deviceName = deviceName,
            os = os,
            osVersion = osVersion,
            appVersion = appVersion,
        )
}
