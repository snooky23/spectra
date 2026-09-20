package com.spectra.logger.feature.streaming.transport

import com.spectra.logger.feature.streaming.model.StreamPacket
import kotlinx.coroutines.flow.Flow

/**
 * Transport abstraction for sending and receiving [StreamPacket] messages over a bidirectional connection.
 */
interface StreamTransport {
    /**
     * Connects to the remote WebSocket endpoint at [url].
     */
    suspend fun connect(url: String)

    /**
     * Sends a packet to the remote endpoint.
     */
    suspend fun send(packet: StreamPacket)

    /**
     * Incoming stream of packets received from the remote endpoint.
     */
    fun receive(): Flow<StreamPacket>

    /**
     * Gracefully closes the connection.
     */
    suspend fun close()
}
