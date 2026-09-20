package com.spectra.logger.feature.streaming.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.streaming.SpectraStreamClient
import com.spectra.logger.feature.streaming.model.StreamConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RemoteStreamUiState(
    val connectionState: StreamConnectionState = StreamConnectionState.DISCONNECTED,
    val activeSessionId: String? = null,
    val serverUrl: String = "ws://192.168.1.100:9292/spectra-ws",
    val token: String = "",
    val errorMessage: String? = null,
)

class RemoteStreamViewModel(
    private val streamClient: SpectraStreamClient = SpectraLogger.streamClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RemoteStreamUiState())
    val uiState: StateFlow<RemoteStreamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            streamClient.connectionState.collect { state ->
                _uiState.update { current ->
                    current.copy(
                        connectionState = state,
                        errorMessage =
                            if (state == StreamConnectionState.ERROR) {
                                "Failed to connect to remote streaming server."
                            } else if (state == StreamConnectionState.REJECTED) {
                                "Connection was rejected by the desktop browser companion."
                            } else {
                                null
                            },
                    )
                }
            }
        }

        viewModelScope.launch {
            streamClient.activeSessionId.collect { sessionId ->
                _uiState.update { it.copy(activeSessionId = sessionId) }
            }
        }
    }

    fun onServerUrlChanged(url: String) {
        _uiState.update { it.copy(serverUrl = url, errorMessage = null) }
    }

    fun onTokenChanged(token: String) {
        _uiState.update { it.copy(token = token, errorMessage = null) }
    }

    /**
     * Parses scanned QR code payload formatted as: `ws://<ip>:<port>/spectra-ws?token=<token>`
     */
    fun onQrCodeScanned(qrContent: String) {
        val trimmed = qrContent.trim()
        if (trimmed.contains("?token=")) {
            val parts = trimmed.split("?token=")
            val url = parts[0]
            val token = parts.getOrNull(1).orEmpty()
            _uiState.update { it.copy(serverUrl = url, token = token, errorMessage = null) }
        } else {
            _uiState.update { it.copy(serverUrl = trimmed, errorMessage = null) }
        }
    }

    fun connect() {
        val state = _uiState.value
        val url = state.serverUrl.trim()
        val token = state.token.trim()

        if (url.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Server URL cannot be empty.") }
            return
        }

        viewModelScope.launch {
            try {
                streamClient.connect(url, token)
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        connectionState = StreamConnectionState.ERROR,
                        errorMessage = e.message ?: "Connection error",
                    )
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            streamClient.disconnect()
        }
    }
}
