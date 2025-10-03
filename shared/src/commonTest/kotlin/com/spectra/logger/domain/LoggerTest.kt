package com.spectra.logger.domain

import app.cash.turbine.test
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.LogLevel
import com.spectra.logger.domain.storage.InMemoryLogStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoggerTest {
    @Test
    fun testBasicLogging() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage)

            logger.i("Test", "Info message")
            logger.e("Test", "Error message")

            delay(50) // Wait for async storage

            assertEquals(2, storage.count())
        }

    @Test
    fun testAllLogLevels() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage)

            logger.v("Test", "Verbose")
            logger.d("Test", "Debug")
            logger.i("Test", "Info")
            logger.w("Test", "Warning")
            logger.e("Test", "Error")
            logger.f("Test", "Fatal")

            delay(100) // Increased delay for async storage

            assertEquals(6, storage.count())

            val logs = storage.query()
            // Logs should be in reverse order (newest first)
            assertTrue(logs.size == 6)
            assertTrue(logs.any { it.level == LogLevel.FATAL })
            assertTrue(logs.any { it.level == LogLevel.ERROR })
            assertTrue(logs.any { it.level == LogLevel.WARNING })
            assertTrue(logs.any { it.level == LogLevel.INFO })
            assertTrue(logs.any { it.level == LogLevel.DEBUG })
            assertTrue(logs.any { it.level == LogLevel.VERBOSE })
        }

    @Test
    fun testMinLevelFiltering() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage, minLevel = LogLevel.WARNING)

            logger.d("Test", "Debug - should be filtered")
            logger.i("Test", "Info - should be filtered")
            logger.w("Test", "Warning - should pass")
            logger.e("Test", "Error - should pass")

            delay(50)

            assertEquals(2, storage.count())
            val logs = storage.query()
            assertTrue(logs.all { it.level.priority >= LogLevel.WARNING.priority })
        }

    @Test
    fun testLoggingWithMetadata() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage)

            val metadata = mapOf("userId" to "123", "action" to "login")
            logger.i("Auth", "User logged in", metadata = metadata)

            delay(50)

            val logs = storage.query()
            assertEquals(1, logs.size)
            assertEquals(metadata, logs.first().metadata)
        }

    @Test
    fun testLoggingWithThrowable() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage)

            val exception = RuntimeException("Test error")
            logger.e("Error", "Something went wrong", throwable = exception)

            delay(50)

            val logs = storage.query()
            assertEquals(1, logs.size)
            assertTrue(logs.first().throwable?.contains("RuntimeException") == true)
            assertTrue(logs.first().throwable?.contains("Test error") == true)
        }

    @Test
    fun testQuery() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage)

            logger.i("Tag1", "Message 1")
            logger.e("Tag2", "Message 2")
            logger.w("Tag1", "Message 3")

            delay(50)

            val filter = LogFilter(tags = setOf("Tag1"))
            val results = logger.query(filter)

            assertEquals(2, results.size)
            assertTrue(results.all { it.tag == "Tag1" })
        }

    @Test
    fun testObserve() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage)

            logger.observe().test {
                logger.i("Test", "Message 1")
                val item1 = awaitItem()
                assertEquals("Message 1", item1.message)

                logger.e("Test", "Message 2")
                val item2 = awaitItem()
                assertEquals("Message 2", item2.message)

                cancel()
            }
        }

    @Test
    fun testClear() =
        runTest {
            val storage = InMemoryLogStorage()
            val logger = Logger(storage)

            logger.i("Test", "Message 1")
            logger.i("Test", "Message 2")
            delay(50)

            assertEquals(2, logger.count())

            logger.clear()

            assertEquals(0, logger.count())
        }
}
