package com.spectra.logger.feature.streaming

import com.spectra.logger.feature.streaming.model.StreamConnectionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Client managing WebSocket connection, pairing handshake, historical sync, and live streaming.
 */
interface SpectraStreamClient {
    /**
     * Observable connection state.
     */
    val connectionState: StateFlow<StreamConnectionState>

    /**
     * Active session ID provided by the desktop companion upon handshake approval.
     */
    val activeSessionId: StateFlow<String?>

    /**
     * Connects to a desktop companion WebSocket at [url] with [token].
     */
    suspend fun connect(
        url: String,
        token: String,
    )

    /**
     * Closes the active streaming session and releases resources.
     */
    suspend fun disconnect()
}
