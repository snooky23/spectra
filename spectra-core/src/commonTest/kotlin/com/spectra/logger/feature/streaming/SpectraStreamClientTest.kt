package com.spectra.logger.feature.streaming

import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.events.model.EventLogEntry
import com.spectra.logger.feature.events.model.EventType
import com.spectra.logger.feature.events.storage.InMemoryEventLogStorage
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogLevel
import com.spectra.logger.feature.logs.storage.InMemoryLogStorage
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.storage.InMemoryNetworkLogStorage
import com.spectra.logger.feature.streaming.model.DeviceInfo
import com.spectra.logger.feature.streaming.model.DeviceInfoProvider
import com.spectra.logger.feature.streaming.model.StreamConnectionState
import com.spectra.logger.feature.streaming.model.StreamPacket
import com.spectra.logger.feature.streaming.transport.StreamTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpectraStreamClientTest {
    private class FakeStreamTransport : StreamTransport {
        var connectedUrl: String? = null
        val sentPackets = mutableListOf<StreamPacket>()
        private val incomingFlow = MutableSharedFlow<StreamPacket>(replay = 10)
        var isClosed = false

        override suspend fun connect(url: String) {
            connectedUrl = url
            isClosed = false
        }

        override suspend fun send(packet: StreamPacket) {
            sentPackets.add(packet)
        }

        override fun receive(): Flow<StreamPacket> = incomingFlow.asSharedFlow()

        suspend fun emitIncoming(packet: StreamPacket) {
            incomingFlow.emit(packet)
        }

        override suspend fun close() {
            isClosed = true
        }
    }

    private class FakeDeviceInfoProvider : DeviceInfoProvider {
        override fun getDeviceInfo(): DeviceInfo =
            DeviceInfo(
                deviceId = "test-device-id",
                deviceName = "Spectra Test Device",
                os = "TestOS",
                osVersion = "1.0",
                appVersion = "1.0.0",
            )
    }

    @Test
    fun testConnectSendsHandshakeAndAwaitsAuthorization() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val testScope = TestScope(testDispatcher)

            val transport = FakeStreamTransport()
            val logStorage = InMemoryLogStorage(maxCapacity = 100)
            val networkStorage = InMemoryNetworkLogStorage(maxCapacity = 100)
            val eventStorage = InMemoryEventLogStorage(maxCapacity = 100)

            val client =
                DefaultSpectraStreamClient(
                    transport = transport,
                    logStorage = logStorage,
                    networkStorage = networkStorage,
                    eventStorage = eventStorage,
                    deviceInfoProvider = FakeDeviceInfoProvider(),
                    coroutineScope = testScope,
                )

            assertEquals(StreamConnectionState.DISCONNECTED, client.connectionState.value)

            client.connect("ws://192.168.1.50:9292/spectra-ws", "token-xyz")
            testScheduler.advanceUntilIdle()

            assertEquals("ws://192.168.1.50:9292/spectra-ws", transport.connectedUrl)
            assertEquals(StreamConnectionState.AWAITING_AUTHORIZATION, client.connectionState.value)

            assertEquals(1, transport.sentPackets.size)
            val handshake = transport.sentPackets.first()
            assertIs<StreamPacket.HandshakeRequest>(handshake)
            assertEquals("test-device-id", handshake.deviceId)
            assertEquals("Spectra Test Device", handshake.deviceName)
            assertEquals("token-xyz", handshake.token)
        }

    @Test
    fun testHandshakeRejected() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val testScope = TestScope(testDispatcher)

            val transport = FakeStreamTransport()
            val client =
                DefaultSpectraStreamClient(
                    transport = transport,
                    logStorage = InMemoryLogStorage(maxCapacity = 100),
                    networkStorage = InMemoryNetworkLogStorage(maxCapacity = 100),
                    eventStorage = InMemoryEventLogStorage(maxCapacity = 100),
                    deviceInfoProvider = FakeDeviceInfoProvider(),
                    coroutineScope = testScope,
                )

            client.connect("ws://192.168.1.50:9292/spectra-ws", "token-xyz")
            testScheduler.advanceUntilIdle()

            transport.emitIncoming(
                StreamPacket.HandshakeResponse(accepted = false, sessionId = "", reason = "User rejected"),
            )
            testScheduler.advanceUntilIdle()

            assertEquals(StreamConnectionState.REJECTED, client.connectionState.value)
            assertTrue(transport.isClosed)
        }

    @Test
    fun testHandshakeAcceptedTriggersCatchUpBatchAndLiveSync() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val testScope = TestScope(testDispatcher)

            val transport = FakeStreamTransport()
            val logStorage = InMemoryLogStorage(maxCapacity = 100)
            val networkStorage = InMemoryNetworkLogStorage(maxCapacity = 100)
            val eventStorage = InMemoryEventLogStorage(maxCapacity = 100)

            val now = SpectraTime.now()
            logStorage.add(LogEntry("l-1", now, LogLevel.INFO, "Tag1", "Existing Log"))
            networkStorage.add(NetworkLogEntry("n-1", now, "https://api.test.com", "GET"))
            eventStorage.add(EventLogEntry("e-1", now, EventType.SCREEN_VIEW, "MainScreen"))

            val client =
                DefaultSpectraStreamClient(
                    transport = transport,
                    logStorage = logStorage,
                    networkStorage = networkStorage,
                    eventStorage = eventStorage,
                    deviceInfoProvider = FakeDeviceInfoProvider(),
                    coroutineScope = testScope,
                )

            client.connect("ws://192.168.1.50:9292/spectra-ws", "token-xyz")
            testScheduler.advanceUntilIdle()

            // Desktop approves connection
            transport.emitIncoming(
                StreamPacket.HandshakeResponse(accepted = true, sessionId = "session-42"),
            )
            testScheduler.advanceUntilIdle()

            assertEquals(StreamConnectionState.STREAMING, client.connectionState.value)
            assertEquals("session-42", client.activeSessionId.value)

            // Verify BatchHistory was sent (Phase 1)
            val batchPacket = transport.sentPackets.firstOrNull { it is StreamPacket.BatchHistory }
            assertIs<StreamPacket.BatchHistory>(batchPacket)
            assertEquals(1, batchPacket.logs.size)
            assertEquals("Existing Log", batchPacket.logs.first().message)
            assertEquals(1, batchPacket.networkLogs.size)
            assertEquals(1, batchPacket.events.size)

            // Verify Live Incremental Stream (Phase 2)
            logStorage.add(LogEntry("l-2", now, LogLevel.WARNING, "Tag2", "Live Log Incoming"))
            networkStorage.add(NetworkLogEntry("n-2", now, "https://api.test.com/post", "POST"))
            eventStorage.add(EventLogEntry("e-2", now, EventType.USER_ACTION, "tap_button"))
            testScheduler.advanceUntilIdle()

            val liveLogs = transport.sentPackets.filterIsInstance<StreamPacket.LiveLog>()
            assertEquals(1, liveLogs.size)
            assertEquals("Live Log Incoming", liveLogs.first().log.message)

            val liveNets = transport.sentPackets.filterIsInstance<StreamPacket.LiveNetwork>()
            assertEquals(1, liveNets.size)
            assertEquals("https://api.test.com/post", liveNets.first().networkLog.url)

            val liveEvts = transport.sentPackets.filterIsInstance<StreamPacket.LiveEvent>()
            assertEquals(1, liveEvts.size)
            assertEquals("tap_button", liveEvts.first().event.name)

            // Test Heartbeat Ping/Pong
            transport.emitIncoming(StreamPacket.Ping)
            testScheduler.advanceUntilIdle()

            val pongs = transport.sentPackets.filterIsInstance<StreamPacket.Pong>()
            assertEquals(1, pongs.size)

            // Test Disconnect
            client.disconnect()
            testScheduler.advanceUntilIdle()

            assertEquals(StreamConnectionState.DISCONNECTED, client.connectionState.value)
            assertTrue(transport.isClosed)
        }
}
