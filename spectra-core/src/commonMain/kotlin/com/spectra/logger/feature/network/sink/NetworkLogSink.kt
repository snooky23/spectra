package com.spectra.logger.feature.network.sink

import com.spectra.logger.feature.network.model.NetworkLogEntry

/**
 * Interface for intercepting and handling network logs captured by Spectra Logger.
 *
 * Sinks are isolated plugins that receive logs asynchronously. If a Sink throws an exception,
 * it will be caught and swallowed by the Logger to ensure the host application does not crash.
 */
interface NetworkLogSink {
    /**
     * Called whenever a new network log is recorded.
     * @param entry The network log entry containing request and response details.
     */
    suspend fun logNetwork(entry: NetworkLogEntry)
}
