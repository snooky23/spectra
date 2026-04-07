package com.spectra.logger.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.model.SourceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID

class NetworkViewModel : ViewModel() {

    fun simulateRequest(method: String, url: String, statusCode: Int?, duration: Double, errorMessage: String? = null) {
        viewModelScope.launch {
            delay((duration * 1000).toLong())
            
            val durationMs = (duration * 1000).toLong()
            
            val requestBody = when (method) {
                "POST" -> "{\"username\": \"testuser\", \"email\": \"test@example.com\"}"
                "PUT" -> "{\"id\": \"123\", \"status\": \"updated\"}"
                else -> null
            }

            val responseHeaders = if (statusCode != null) mapOf(
                "Content-Type" to "application/json",
                "X-Response-Time" to "${durationMs}ms"
            ) else emptyMap()

            val responseBody = when (statusCode) {
                200 -> "{\"success\": true, \"data\": {\"id\": \"123\"}}"
                201 -> "{\"success\": true, \"data\": {\"id\": \"123\"}}"
                204 -> null
                404 -> "{\"error\": \"Not Found\"}"
                429 -> "{\"error\": \"Too Many Requests\"}"
                500 -> "{\"error\": \"Internal Server Error\"}"
                null -> null
                else -> "{\"status\": $statusCode}"
            }

            val errorText = errorMessage ?: if (statusCode != null && statusCode >= 400) "HTTP $statusCode: Request failed" else null

            val entry = NetworkLogEntry(
                id = UUID.randomUUID().toString(),
                timestamp = Clock.System.now(),
                url = url,
                method = method,
                requestHeaders = mapOf("Accept" to "application/json"),
                requestBody = requestBody,
                responseCode = statusCode,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                duration = durationMs,
                error = errorText,
                source = "SpectraExample",
                sourceType = SourceType.APP
            )

            SpectraLogger.networkStorage.add(entry)
        }
    }

    fun simulate10ApiCalls() {
        val endpoints = listOf("users", "orders", "products", "inventory", "analytics")
        val statuses = listOf(200, 200, 200, 404, 500)
        
        viewModelScope.launch {
            for (i in 0 until 10) {
                delay(200)
                simulateRequest(
                    method = if (i % 3 == 0) "POST" else "GET",
                    url = "https://api.example.com/${endpoints[i % endpoints.size]}",
                    statusCode = statuses[i % statuses.size],
                    duration = 0.2 + (i * 0.1)
                )
            }
        }
    }
}
