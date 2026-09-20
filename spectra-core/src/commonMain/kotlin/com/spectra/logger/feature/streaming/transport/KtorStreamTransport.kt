package com.spectra.logger.feature.streaming.transport

import com.spectra.logger.feature.streaming.model.StreamPacket
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Ktor multiplatform WebSocket implementation of [StreamTransport].
 */
class KtorStreamTransport(
    private val client: HttpClient =
        HttpClient {
            install(WebSockets)
        },
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
        },
) : StreamTransport {
    private var session: DefaultClientWebSocketSession? = null

    override suspend fun connect(url: String) {
        session = client.webSocketSession(urlString = url)
    }

    override suspend fun send(packet: StreamPacket) {
        val currentSession = session ?: error("Not connected to WebSocket")
        val jsonText = json.encodeToString(packet)
        currentSession.send(Frame.Text(jsonText))
    }

    override fun receive(): Flow<StreamPacket> {
        val currentSession = session ?: error("Not connected to WebSocket")
        return currentSession.incoming
            .receiveAsFlow()
            .filterIsInstance<Frame.Text>()
            .map { frame ->
                json.decodeFromString<StreamPacket>(frame.readText())
            }
    }

    override suspend fun close() {
        try {
            session?.close()
        } catch (_: Throwable) {
            // Ignore close exceptions
        }
        session = null
    }
}
