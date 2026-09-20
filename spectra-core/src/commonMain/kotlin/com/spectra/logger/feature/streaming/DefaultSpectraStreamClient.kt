package com.spectra.logger.feature.streaming

import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.events.storage.EventLogStorage
import com.spectra.logger.feature.logs.storage.LogStorage
import com.spectra.logger.feature.network.storage.NetworkLogStorage
import com.spectra.logger.feature.streaming.model.DeviceInfoProvider
import com.spectra.logger.feature.streaming.model.SimpleDeviceInfoProvider
import com.spectra.logger.feature.streaming.model.StreamConnectionState
import com.spectra.logger.feature.streaming.model.StreamPacket
import com.spectra.logger.feature.streaming.transport.KtorStreamTransport
import com.spectra.logger.feature.streaming.transport.StreamTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Production implementation of [SpectraStreamClient] implementing the two-phase synchronization protocol:
 * 1. Historical catch-up batch sync upon handshake authorization
 * 2. Real-time incremental streaming of incoming logs, network calls, and events
 */
class DefaultSpectraStreamClient(
    private val transport: StreamTransport = KtorStreamTransport(),
    private val logStorage: LogStorage = SpectraLogger.logStorage,
    private val networkStorage: NetworkLogStorage = SpectraLogger.networkStorage,
    private val eventStorage: EventLogStorage = SpectraLogger.eventStorage,
    private val deviceInfoProvider: DeviceInfoProvider = SimpleDeviceInfoProvider(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : SpectraStreamClient {
    private val mutex = Mutex()
    private val _connectionState = MutableStateFlow(StreamConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<StreamConnectionState> = _connectionState.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String?>(null)
    override val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val activeJobs = mutableListOf<Job>()
    private var listenerJob: Job? = null

    override suspend fun connect(
        url: String,
        token: String,
    ) = mutex.withLock {
        if (_connectionState.value == StreamConnectionState.STREAMING ||
            _connectionState.value == StreamConnectionState.CONNECTING
        ) {
            return@withLock
        }

        cleanupJobs()
        _connectionState.value = StreamConnectionState.CONNECTING

        try {
            transport.connect(url)

            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            val handshakeRequest =
                StreamPacket.HandshakeRequest(
                    deviceId = deviceInfo.deviceId,
                    deviceName = deviceInfo.deviceName,
                    os = deviceInfo.os,
                    osVersion = deviceInfo.osVersion,
                    appVersion = deviceInfo.appVersion,
                    token = token,
                )
            transport.send(handshakeRequest)
            _connectionState.value = StreamConnectionState.AWAITING_AUTHORIZATION

            listenerJob =
                coroutineScope.launch {
                    try {
                        transport.receive().collect { packet ->
                            handleIncomingPacket(packet)
                        }
                        // Connection closed gracefully by remote
                        if (_connectionState.value == StreamConnectionState.STREAMING) {
                            disconnectInternal()
                        }
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        _connectionState.value = StreamConnectionState.ERROR
                        disconnectInternal()
                    }
                }
        } catch (_: Throwable) {
            _connectionState.value = StreamConnectionState.ERROR
            cleanupJobs()
            transport.close()
        }
    }

    private suspend fun handleIncomingPacket(packet: StreamPacket) {
        when (packet) {
            is StreamPacket.HandshakeResponse -> {
                if (packet.accepted) {
                    _activeSessionId.value = packet.sessionId
                    _connectionState.value = StreamConnectionState.STREAMING
                    startTwoPhaseStreaming()
                } else {
                    _connectionState.value = StreamConnectionState.REJECTED
                    disconnectInternal(cancelListener = false)
                }
            }
            is StreamPacket.Ping -> {
                try {
                    transport.send(StreamPacket.Pong)
                } catch (_: Throwable) {
                    // Ignored on close
                }
            }
            else -> {
                // Client primarily sends telemetry; incoming client packets ignored or handled by server
            }
        }
    }

    private suspend fun startTwoPhaseStreaming() {
        // Phase 1: Catch-up historical sync
        try {
            val logs = logStorage.query()
            val network = networkStorage.query()
            val events = eventStorage.query()

            transport.send(
                StreamPacket.BatchHistory(
                    sequence = 1,
                    isFinalBatch = true,
                    logs = logs,
                    networkLogs = network,
                    events = events,
                ),
            )
        } catch (_: Throwable) {
            _connectionState.value = StreamConnectionState.ERROR
            return
        }

        // Phase 2: Real-time incremental streams
        val logJob =
            coroutineScope.launch {
                logStorage.observe().collect { logEntry ->
                    if (_connectionState.value == StreamConnectionState.STREAMING) {
                        try {
                            transport.send(StreamPacket.LiveLog(logEntry))
                        } catch (_: Throwable) {
                            // Drop or handle disconnect
                        }
                    }
                }
            }
        activeJobs.add(logJob)

        val netJob =
            coroutineScope.launch {
                networkStorage.observe().collect { netEntry ->
                    if (_connectionState.value == StreamConnectionState.STREAMING) {
                        try {
                            transport.send(StreamPacket.LiveNetwork(netEntry))
                        } catch (_: Throwable) {
                            // Drop or handle disconnect
                        }
                    }
                }
            }
        activeJobs.add(netJob)

        val eventJob =
            coroutineScope.launch {
                eventStorage.observe().collect { eventEntry ->
                    if (_connectionState.value == StreamConnectionState.STREAMING) {
                        try {
                            transport.send(StreamPacket.LiveEvent(eventEntry))
                        } catch (_: Throwable) {
                            // Drop or handle disconnect
                        }
                    }
                }
            }
        activeJobs.add(eventJob)
    }

    override suspend fun disconnect() =
        mutex.withLock {
            disconnectInternal(cancelListener = true)
        }

    private suspend fun disconnectInternal(cancelListener: Boolean = true) {
        cleanupJobs(cancelListener = cancelListener)
        try {
            transport.close()
        } catch (_: Throwable) {
        }

        if (_connectionState.value != StreamConnectionState.REJECTED &&
            _connectionState.value != StreamConnectionState.ERROR
        ) {
            _connectionState.value = StreamConnectionState.DISCONNECTED
        }
        _activeSessionId.value = null
    }

    private fun cleanupJobs(cancelListener: Boolean = true) {
        if (cancelListener) {
            listenerJob?.cancel()
            listenerJob = null
        }
        activeJobs.forEach { it.cancel() }
        activeJobs.clear()
    }
}
