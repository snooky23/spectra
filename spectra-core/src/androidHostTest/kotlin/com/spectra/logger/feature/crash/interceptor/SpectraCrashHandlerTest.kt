package com.spectra.logger.feature.crash.interceptor

import com.spectra.logger.feature.crash.model.Breadcrumb
import com.spectra.logger.feature.crash.model.BreadcrumbType
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.storage.InMemoryCrashStorage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class SpectraCrashHandlerTest {
    @Test
    fun testCrashInterceptionAndHandlerChaining() =
        runTest {
            val storage = InMemoryCrashStorage()
            val breadcrumbRecorder = DefaultBreadcrumbRecorder()
            breadcrumbRecorder.record(
                Breadcrumb(
                    timestamp = 1000L,
                    type = BreadcrumbType.LOG,
                    category = "Test",
                    message = "About to crash",
                ),
            )

            var callbackReport: CrashReport? = null
            val defaultHandlerInvoked = AtomicBoolean(false)

            val mockDefaultHandler =
                Thread.UncaughtExceptionHandler { thread, throwable ->
                    defaultHandlerInvoked.set(true)
                    assertEquals("test-thread", thread.name)
                    assertEquals("Simulated crash message", throwable.message)
                }

            val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
            try {
                Thread.setDefaultUncaughtExceptionHandler(mockDefaultHandler)

                val crashHandler =
                    SpectraCrashHandler(
                        crashStorage = storage,
                        breadcrumbRecorder = breadcrumbRecorder,
                        deviceInfoProvider = { mapOf("device" to "TestPhone") },
                        onCrashRecorded = { callbackReport = it },
                    )

                crashHandler.install()
                assertTrue(crashHandler.isInstalled())

                val testThread = Thread({ }, "test-thread")
                val testException = IllegalStateException("Simulated crash message")

                crashHandler.uncaughtException(testThread, testException)

                // Verify default handler was chained
                assertTrue(defaultHandlerInvoked.get())

                // Verify crash was recorded to storage
                val recorded = storage.getLatestCrash()
                assertNotNull(recorded)
                assertEquals("java.lang.IllegalStateException", recorded?.exceptionClass)
                assertEquals("Simulated crash message", recorded?.message)
                assertEquals("test-thread", recorded?.threadName)
                assertEquals("TestPhone", recorded?.metadata?.get("device"))
                assertEquals(1, recorded?.breadcrumbs?.size)
                assertEquals("About to crash", recorded?.breadcrumbs?.first()?.message)

                // Verify callback was invoked
                assertNotNull(callbackReport)
                assertEquals(recorded?.id, callbackReport?.id)

                // Test uninstall
                crashHandler.uninstall()
                assertEquals(mockDefaultHandler, Thread.getDefaultUncaughtExceptionHandler())
            } finally {
                Thread.setDefaultUncaughtExceptionHandler(originalHandler)
            }
        }
}
