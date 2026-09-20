package com.spectra.logger.feature.streaming.ui

import com.spectra.logger.feature.streaming.SpectraStreamClient
import com.spectra.logger.feature.streaming.model.StreamConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteStreamViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private class FakeSpectraStreamClient : SpectraStreamClient {
        private val _connectionState = MutableStateFlow(StreamConnectionState.DISCONNECTED)
        override val connectionState: StateFlow<StreamConnectionState> = _connectionState.asStateFlow()

        private val _activeSessionId = MutableStateFlow<String?>(null)
        override val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

        var lastConnectedUrl: String? = null
        var lastConnectedToken: String? = null
        var disconnectCalled = false

        override suspend fun connect(
            url: String,
            token: String,
        ) {
            lastConnectedUrl = url
            lastConnectedToken = token
            _connectionState.value = StreamConnectionState.STREAMING
            _activeSessionId.value = "session-100"
        }

        override suspend fun disconnect() {
            disconnectCalled = true
            _connectionState.value = StreamConnectionState.DISCONNECTED
            _activeSessionId.value = null
        }

        fun setState(state: StreamConnectionState) {
            _connectionState.value = state
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testQrCodeParsing() {
        val fakeClient = FakeSpectraStreamClient()
        val viewModel = RemoteStreamViewModel(streamClient = fakeClient)

        viewModel.onQrCodeScanned("ws://192.168.1.150:9292/spectra-ws?token=my-secret-key-123")

        val state = viewModel.uiState.value
        assertEquals("ws://192.168.1.150:9292/spectra-ws", state.serverUrl)
        assertEquals("my-secret-key-123", state.token)
        assertNull(state.errorMessage)
    }

    @Test
    fun testConnectAndDisconnect() =
        runTest(testDispatcher) {
            val fakeClient = FakeSpectraStreamClient()
            val viewModel = RemoteStreamViewModel(streamClient = fakeClient)

            viewModel.onServerUrlChanged("ws://10.0.0.5:9292/spectra-ws")
            viewModel.onTokenChanged("token-abc")
            viewModel.connect()
            advanceUntilIdle()

            assertEquals("ws://10.0.0.5:9292/spectra-ws", fakeClient.lastConnectedUrl)
            assertEquals("token-abc", fakeClient.lastConnectedToken)
            assertEquals(StreamConnectionState.STREAMING, viewModel.uiState.value.connectionState)
            assertEquals("session-100", viewModel.uiState.value.activeSessionId)

            viewModel.disconnect()
            advanceUntilIdle()

            assertTrue(fakeClient.disconnectCalled)
            assertEquals(StreamConnectionState.DISCONNECTED, viewModel.uiState.value.connectionState)
            assertNull(viewModel.uiState.value.activeSessionId)
        }

    @Test
    fun testRejectionErrorMessage() =
        runTest(testDispatcher) {
            val fakeClient = FakeSpectraStreamClient()
            val viewModel = RemoteStreamViewModel(streamClient = fakeClient)

            fakeClient.setState(StreamConnectionState.REJECTED)
            advanceUntilIdle()

            assertEquals(StreamConnectionState.REJECTED, viewModel.uiState.value.connectionState)
            assertTrue(viewModel.uiState.value.errorMessage!!.contains("rejected by the desktop"))
        }
}
