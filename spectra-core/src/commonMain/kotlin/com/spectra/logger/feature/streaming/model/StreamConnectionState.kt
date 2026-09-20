package com.spectra.logger.feature.streaming.model

import kotlinx.serialization.Serializable

/**
 * State of the remote streaming connection.
 */
@Serializable
enum class StreamConnectionState {
    DISCONNECTED,
    CONNECTING,
    AWAITING_AUTHORIZATION,
    STREAMING,
    REJECTED,
    ERROR,
}
