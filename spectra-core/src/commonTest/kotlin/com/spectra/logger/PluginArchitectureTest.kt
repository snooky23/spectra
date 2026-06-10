package com.spectra.logger

import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.sink.LogSink
import com.spectra.logger.feature.network.model.NetworkLogEntry
import com.spectra.logger.feature.network.sink.NetworkLogSink
import com.spectra.logger.feature.settings.config.configure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PluginArchitectureTest {
    @AfterTest
    fun tearDown() =
        runTest {
            SpectraLogger.setCoroutineScopeForTesting(this)
            SpectraLogger.clear()
            SpectraLogger.clearNetwork()
            SpectraLogger.configure {}
            SpectraLogger.resetCoroutineScopeForTesting()
        }

    @Test
    fun testCustomLogSinkReceivesLogs() =
        runTest {
            SpectraLogger.setCoroutineScopeForTesting(this)
            val receivedEntries = mutableListOf<LogEntry>()
            val customSink =
                object : LogSink {
                    override suspend fun log(entry: LogEntry) {
                        receivedEntries.add(entry)
                    }
                }

            SpectraLogger.configure {
                addSink(customSink)
            }

            SpectraLogger.d("TestTag", "Hello custom sink!")
            runCurrent()

            assertEquals(1, receivedEntries.size)
            assertEquals("TestTag", receivedEntries.first().tag)
            assertEquals("Hello custom sink!", receivedEntries.first().message)
        }

    @Test
    fun testCustomNetworkLogSinkReceivesLogs() =
        runTest {
            SpectraLogger.setCoroutineScopeForTesting(this)
            val receivedEntries = mutableListOf<NetworkLogEntry>()
            val customSink =
                object : NetworkLogSink {
                    override suspend fun logNetwork(entry: NetworkLogEntry) {
                        receivedEntries.add(entry)
                    }
                }

            SpectraLogger.configure {
                addNetworkSink(customSink)
            }

            val dummyNetworkEntry =
                NetworkLogEntry(
                    id = "net-1",
                    timestamp = SpectraTime.now(),
                    method = "GET",
                    url = "https://example.com/api",
                    requestHeaders = emptyMap(),
                    requestBody = null,
                    responseHeaders = emptyMap(),
                    responseCode = 200,
                    responseBody = null,
                    duration = 50L,
                )

            SpectraLogger.logNetwork(dummyNetworkEntry)
            runCurrent()

            assertEquals(1, receivedEntries.size)
            assertEquals("GET", receivedEntries.first().method)
            assertEquals("https://example.com/api", receivedEntries.first().url)
        }

    @Test
    fun testCrashingLogSinkDoesNotCrashHost() =
        runTest {
            SpectraLogger.setCoroutineScopeForTesting(this)
            var crashSinkCalled = false
            val crashingSink =
                object : LogSink {
                    override suspend fun log(entry: LogEntry) {
                        crashSinkCalled = true
                        throw RuntimeException("Malicious plugin crash!")
                    }
                }

            val receivedEntries = mutableListOf<LogEntry>()
            val stableSink =
                object : LogSink {
                    override suspend fun log(entry: LogEntry) {
                        receivedEntries.add(entry)
                    }
                }

            SpectraLogger.configure {
                addSink(crashingSink)
                addSink(stableSink)
            }

            // This should NOT crash the test suite
            SpectraLogger.i("CrashTest", "Testing resiliency")
            runCurrent()

            assertTrue(crashSinkCalled, "Crashing sink should have been invoked")
            assertEquals(1, receivedEntries.size, "Stable sink should still receive logs")
            assertEquals(1, SpectraLogger.count(), "Local storage should still persist the log")
        }
}
