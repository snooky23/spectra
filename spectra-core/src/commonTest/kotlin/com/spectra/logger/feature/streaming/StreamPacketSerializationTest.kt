package com.spectra.logger.feature.streaming

import com.spectra.logger.core.model.SourceType
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.streaming.model.StreamPacket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StreamPacketSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }

    @Test
    fun testHandshakeRequestSerialization() {
        val original: StreamPacket =
            StreamPacket.HandshakeRequest(
                deviceId = "pixel-7-123",
                deviceName = "Google Pixel 7",
                os = "Android",
                osVersion = "14",
                appVersion = "1.0.0",
                token = "secret-token-xyz",
            )

        val encoded = json.encodeToString(original)
        assertTrue(encoded.contains("\"type\":\"handshake_request\""))
        assertTrue(encoded.contains("\"deviceName\":\"Google Pixel 7\""))

        val decoded = json.decodeFromString<StreamPacket>(encoded)
        assertIs<StreamPacket.HandshakeRequest>(decoded)
        assertEquals("pixel-7-123", decoded.deviceId)
        assertEquals("secret-token-xyz", decoded.token)
    }

    @Test
    fun testHandshakeResponseSerialization() {
        val accepted: StreamPacket =
            StreamPacket.HandshakeResponse(
                accepted = true,
                sessionId = "session-999",
            )

        val encodedAccepted = json.encodeToString(accepted)
        val decodedAccepted = json.decodeFromString<StreamPacket>(encodedAccepted)
        assertIs<StreamPacket.HandshakeResponse>(decodedAccepted)
        assertTrue(decodedAccepted.accepted)
        assertEquals("session-999", decodedAccepted.sessionId)

        val rejected: StreamPacket =
            StreamPacket.HandshakeResponse(
                accepted = false,
                sessionId = "",
                reason = "Unauthorized user rejected connection",
            )

        val encodedRejected = json.encodeToString(rejected)
        val decodedRejected = json.decodeFromString<StreamPacket>(encodedRejected)
        assertIs<StreamPacket.HandshakeResponse>(decodedRejected)
        assertEquals(false, decodedRejected.accepted)
        assertEquals("Unauthorized user rejected connection", decodedRejected.reason)
    }

    @Test
    fun testBatchHistorySerialization() {
        val now = SpectraTime.now()

        val log =
            LogEntry(
                id = "log-1",
                timestamp = now,
                level = LogLevel.INFO,
                tag = "TestTag",
                message = "Batch log message",
                source = "com.test",
                sourceType = SourceType.APP,
            )

        val netLog =
            NetworkLogEntry(
                id = "net-1",
                timestamp = now,
                url = "https://api.example.com/users",
                method = "GET",
                responseCode = 200,
                duration = 120,
            )

        val event =
            EventLogEntry(
                id = "evt-1",
                timestamp = now,
                eventType = EventType.SCREEN_VIEW,
                name = "HomeScreen",
            )

        val original: StreamPacket =
            StreamPacket.BatchHistory(
                sequence = 1,
                isFinalBatch = true,
                logs = listOf(log),
                networkLogs = listOf(netLog),
                events = listOf(event),
            )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<StreamPacket>(encoded)
        assertIs<StreamPacket.BatchHistory>(decoded)
        assertEquals(1, decoded.sequence)
        assertTrue(decoded.isFinalBatch)
        assertEquals(1, decoded.logs.size)
        assertEquals("Batch log message", decoded.logs.first().message)
        assertEquals(1, decoded.networkLogs.size)
        assertEquals("https://api.example.com/users", decoded.networkLogs.first().url)
        assertEquals(1, decoded.events.size)
        assertEquals("HomeScreen", decoded.events.first().name)
    }

    @Test
    fun testLivePacketsSerialization() {
        val now = SpectraTime.now()

        val liveLog: StreamPacket =
            StreamPacket.LiveLog(
                LogEntry("l-1", now, LogLevel.DEBUG, "Tag", "Live message"),
            )
        val decodedLog = json.decodeFromString<StreamPacket>(json.encodeToString(liveLog))
        assertIs<StreamPacket.LiveLog>(decodedLog)
        assertEquals("Live message", decodedLog.log.message)

        val liveNet: StreamPacket =
            StreamPacket.LiveNetwork(
                NetworkLogEntry("n-1", now, "https://example.com", "POST"),
            )
        val decodedNet = json.decodeFromString<StreamPacket>(json.encodeToString(liveNet))
        assertIs<StreamPacket.LiveNetwork>(decodedNet)
        assertEquals("POST", decodedNet.networkLog.method)

        val liveEvt: StreamPacket =
            StreamPacket.LiveEvent(
                EventLogEntry("e-1", now, EventType.USER_ACTION, "tap_button"),
            )
        val decodedEvt = json.decodeFromString<StreamPacket>(json.encodeToString(liveEvt))
        assertIs<StreamPacket.LiveEvent>(decodedEvt)
        assertEquals("tap_button", decodedEvt.event.name)
    }

    @Test
    fun testPingPongSerialization() {
        val ping: StreamPacket = StreamPacket.Ping
        val decodedPing = json.decodeFromString<StreamPacket>(json.encodeToString(ping))
        assertIs<StreamPacket.Ping>(decodedPing)

        val pong: StreamPacket = StreamPacket.Pong
        val decodedPong = json.decodeFromString<StreamPacket>(json.encodeToString(pong))
        assertIs<StreamPacket.Pong>(decodedPong)
    }
}
