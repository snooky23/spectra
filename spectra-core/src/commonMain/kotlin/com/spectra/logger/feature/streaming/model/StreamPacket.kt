package com.spectra.logger.feature.streaming.model

import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.network.model.NetworkLogEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sealed hierarchy of messages exchanged across WebSocket during remote streaming sessions.
 */
@Serializable
sealed class StreamPacket {
    @Serializable
    @SerialName("handshake_request")
    data class HandshakeRequest(
        val deviceId: String,
        val deviceName: String,
        val os: String,
        val osVersion: String,
        val appVersion: String,
        val token: String,
    ) : StreamPacket()

    @Serializable
    @SerialName("handshake_response")
    data class HandshakeResponse(
        val accepted: Boolean,
        val sessionId: String,
        val reason: String? = null,
    ) : StreamPacket()

    @Serializable
    @SerialName("batch_history")
    data class BatchHistory(
        val sequence: Int,
        val isFinalBatch: Boolean,
        val logs: List<LogEntry> = emptyList(),
        val networkLogs: List<NetworkLogEntry> = emptyList(),
        val events: List<EventLogEntry> = emptyList(),
    ) : StreamPacket()

    @Serializable
    @SerialName("live_log")
    data class LiveLog(
        val log: LogEntry,
    ) : StreamPacket()

    @Serializable
    @SerialName("live_network")
    data class LiveNetwork(
        val networkLog: NetworkLogEntry,
    ) : StreamPacket()

    @Serializable
    @SerialName("live_event")
    data class LiveEvent(
        val event: EventLogEntry,
    ) : StreamPacket()

    @Serializable
    @SerialName("ping")
    data object Ping : StreamPacket()

    @Serializable
    @SerialName("pong")
    data object Pong : StreamPacket()
}
