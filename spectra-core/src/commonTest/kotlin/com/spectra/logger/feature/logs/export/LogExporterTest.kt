package com.spectra.logger.feature.logs.export

import com.spectra.logger.core.model.SourceType
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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LogExporterTest {
    @Test
    fun testExportLogsFormats() =
        runTest {
            val storage = InMemoryLogStorage(maxCapacity = 100)
            storage.add(
                LogEntry(
                    id = "1",
                    timestamp = SpectraTime.now(),
                    level = LogLevel.INFO,
                    tag = "AuthTag",
                    message = "User logged in successfully",
                    metadata = mapOf("userId" to "user-123"),
                ),
            )
            storage.add(
                LogEntry(
                    id = "2",
                    timestamp = SpectraTime.now(),
                    level = LogLevel.ERROR,
                    tag = "PaymentTag",
                    message = "Card declined",
                    throwable = "CardException: Insufficient funds",
                ),
            )

            val text = LogExporter.exportLogsAsText(storage)
            assertTrue(text.contains("Total logs: 2"))
            assertTrue(text.contains("AuthTag"))
            assertTrue(text.contains("Card declined"))

            val json = LogExporter.exportLogsAsJson(storage)
            assertTrue(json.contains("\"totalLogs\": 2"))
            assertTrue(json.contains("\"tag\": \"AuthTag\""))
            assertTrue(json.contains("\"userId\": \"user-123\""))
            assertTrue(json.contains("\"throwable\": \"CardException: Insufficient funds\""))

            val csv = LogExporter.exportLogsAsCsv(storage)
            assertTrue(csv.contains("Timestamp,Level,Tag,Message,Throwable"))
            assertTrue(csv.contains("INFO,AuthTag,User logged in successfully"))
            assertTrue(csv.contains("ERROR,PaymentTag,Card declined,CardException: Insufficient funds"))

            val md = LogExporter.exportLogsAsMarkdown(storage)
            assertTrue(md.contains("# Spectra Application Logs Report"))
            assertTrue(md.contains("| `INFO` | `AuthTag` | User logged in successfully |"))
        }

    @Test
    fun testExportNetworkLogsAsHar() =
        runTest {
            val storage = InMemoryNetworkLogStorage(maxCapacity = 100)
            storage.add(
                NetworkLogEntry(
                    id = "net-1",
                    timestamp = SpectraTime.now(),
                    url = "https://api.example.com/v1/users?limit=10&sort=desc",
                    method = "GET",
                    requestHeaders = mapOf("Authorization" to "Bearer token123", "Accept" to "application/json"),
                    responseCode = 200,
                    responseHeaders = mapOf("Content-Type" to "application/json"),
                    responseBody = "{\"users\": [\"alice\", \"bob\"]}",
                    duration = 145,
                ),
            )
            storage.add(
                NetworkLogEntry(
                    id = "net-2",
                    timestamp = SpectraTime.now(),
                    url = "https://api.example.com/v1/orders",
                    method = "POST",
                    requestHeaders = mapOf("Content-Type" to "application/json"),
                    requestBody = "{\"item\": \"laptop\"}",
                    responseCode = 500,
                    responseHeaders = mapOf("Content-Type" to "application/json"),
                    responseBody = "{\"error\": \"Database failure\"}",
                    duration = 320,
                    error = "Internal Server Error",
                ),
            )

            val har = LogExporter.exportNetworkLogsAsHar(storage)

            // Assert standard HAR 1.2 specifications
            assertTrue(har.contains("\"version\": \"1.2\""))
            assertTrue(har.contains("\"name\": \"Spectra Logger\""))
            assertTrue(har.contains("\"method\": \"GET\""))
            assertTrue(har.contains("\"url\": \"https://api.example.com/v1/users?limit=10&sort=desc\""))
            assertTrue(har.contains("\"status\": 200"))
            assertTrue(har.contains("\"statusText\": \"OK\""))
            assertTrue(har.contains("\"status\": 500"))
            assertTrue(har.contains("\"time\": 145"))
            assertTrue(har.contains("\"limit\""))
            assertTrue(har.contains("\"sort\""))
            assertTrue(har.contains("\"postData\""))
            assertTrue(har.contains("laptop"))
        }

    @Test
    fun testExportEventsFormats() =
        runTest {
            val storage = InMemoryEventLogStorage(maxCapacity = 100)
            storage.add(
                EventLogEntry(
                    id = "evt-1",
                    timestamp = SpectraTime.now(),
                    eventType = EventType.SCREEN_VIEW,
                    name = "CheckoutScreen",
                    parameters = mapOf("cartValue" to "$99.99"),
                    durationMs = 4500,
                    source = "com.spectra.demo",
                    sourceType = SourceType.APP,
                ),
            )
            storage.add(
                EventLogEntry(
                    id = "evt-2",
                    timestamp = SpectraTime.now(),
                    eventType = EventType.USER_ACTION,
                    name = "pay_button_click",
                    parameters = mapOf("method" to "apple_pay"),
                    source = "com.spectra.demo",
                ),
            )

            val text = LogExporter.exportEventsAsText(storage)
            assertTrue(text.contains("Total events: 2"))
            assertTrue(text.contains("[SCREEN_VIEW] CheckoutScreen (4500ms)"))
            assertTrue(text.contains("pay_button_click"))

            val json = LogExporter.exportEventsAsJson(storage)
            assertTrue(json.contains("\"totalEvents\": 2"))
            assertTrue(json.contains("\"type\": \"SCREEN_VIEW\""))
            assertTrue(json.contains("\"name\": \"CheckoutScreen\""))
            assertTrue(json.contains("\"durationMs\": 4500"))
            assertTrue(json.contains("\"cartValue\": \"$99.99\""))

            val csv = LogExporter.exportEventsAsCsv(storage)
            assertTrue(csv.contains("Timestamp,Type,Name,DurationMs,Source,Parameters"))
            assertTrue(csv.contains("SCREEN_VIEW,CheckoutScreen,4500,com.spectra.demo,cartValue=$99.99"))

            val md = LogExporter.exportEventsAsMarkdown(storage)
            assertTrue(md.contains("# Spectra Events & User Analytics Report"))
            assertTrue(md.contains("| `SCREEN_VIEW` | `CheckoutScreen` | 4500ms | `com.spectra.demo` |"))
        }

    @Test
    fun testExportFullBundle() =
        runTest {
            val logStorage = InMemoryLogStorage(maxCapacity = 10)
            val netStorage = InMemoryNetworkLogStorage(maxCapacity = 10)
            val eventStorage = InMemoryEventLogStorage(maxCapacity = 10)

            logStorage.add(LogEntry("l1", SpectraTime.now(), LogLevel.INFO, "App", "App started"))
            netStorage.add(
                NetworkLogEntry(
                    id = "n1",
                    timestamp = SpectraTime.now(),
                    url = "https://example.com/init",
                    method = "GET",
                    responseCode = 200,
                ),
            )
            eventStorage.add(
                EventLogEntry(
                    id = "e1",
                    timestamp = SpectraTime.now(),
                    eventType = EventType.LIFECYCLE,
                    name = "app_foreground",
                ),
            )

            val deviceInfo =
                DeviceInfo(
                    deviceId = "test-123",
                    deviceName = "Pixel 8 Pro",
                    os = "Android",
                    osVersion = "14",
                    appVersion = "2.1.0",
                )

            val bundleJson = LogExporter.exportFullBundleAsJson(logStorage, netStorage, eventStorage, deviceInfo)
            assertTrue(bundleJson.contains("\"bundleTimestamp\""))
            assertTrue(bundleJson.contains("\"deviceName\": \"Pixel 8 Pro\""))
            assertTrue(bundleJson.contains("\"applicationLogs\""))
            assertTrue(bundleJson.contains("\"networkTelemetry\""))
            assertTrue(bundleJson.contains("\"userEvents\""))
            assertTrue(bundleJson.contains("App started"))
            assertTrue(bundleJson.contains("https://example.com/init"))
            assertTrue(bundleJson.contains("app_foreground"))

            val bundleMd = LogExporter.exportFullBundleAsMarkdown(logStorage, netStorage, eventStorage, deviceInfo)
            assertTrue(bundleMd.contains("# Spectra Complete Telemetry Debug Report"))
            assertTrue(bundleMd.contains("Pixel 8 Pro (Android 14)"))
            assertTrue(bundleMd.contains("| **Application Logs** | 1 | 0 errors |"))
            assertTrue(bundleMd.contains("| **Network Requests** | 1 | 0 failed |"))
            assertTrue(bundleMd.contains("| **User Events** | 1 | 0 screen views |"))
        }
}
