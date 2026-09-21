package com.spectra.logger.feature.crash.storage

import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.feature.crash.model.Breadcrumb
import com.spectra.logger.feature.crash.model.BreadcrumbType
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CrashStorageTest {
    @Test
    fun testInMemoryCrashStorageOperations() =
        runTest {
            val storage = InMemoryCrashStorage(maxCapacity = 3)
            assertEquals(0, storage.count())
            assertNull(storage.getLatestCrash())

            val crash1 =
                CrashReport(
                    id = "c1",
                    timestamp = 1000L,
                    exceptionClass = "java.lang.NullPointerException",
                    message = "Null reference",
                    stackTrace = "stack trace 1",
                    severity = CrashSeverity.FATAL,
                    breadcrumbs =
                        listOf(
                            Breadcrumb(
                                timestamp = 999L,
                                type = BreadcrumbType.LOG,
                                category = "Test",
                                message = "Doing test",
                            ),
                        ),
                )
            storage.recordCrash(crash1)

            assertEquals(1, storage.count())
            assertEquals("c1", storage.getLatestCrash()?.id)

            val crash2 =
                CrashReport(
                    id = "c2",
                    timestamp = 2000L,
                    exceptionClass = "java.lang.IllegalStateException",
                    message = "Bad state",
                    stackTrace = "stack trace 2",
                    severity = CrashSeverity.NON_FATAL,
                )
            val crash3 =
                CrashReport(
                    id = "c3",
                    timestamp = 3000L,
                    exceptionClass = "java.lang.IllegalArgumentException",
                    message = "Invalid arg",
                    stackTrace = "stack trace 3",
                )
            val crash4 =
                CrashReport(
                    id = "c4",
                    timestamp = 4000L,
                    exceptionClass = "java.lang.IndexOutOfBoundsException",
                    message = "Out of bounds",
                    stackTrace = "stack trace 4",
                )

            storage.recordCrash(crash2)
            storage.recordCrash(crash3)
            storage.recordCrash(crash4) // Exceeds capacity 3 -> c1 should be evicted

            assertEquals(3, storage.count())
            val crashes = storage.getCrashes()
            assertEquals(listOf("c4", "c3", "c2"), crashes.map { it.id })

            val limited = storage.getCrashes(limit = 2)
            assertEquals(listOf("c4", "c3"), limited.map { it.id })

            val observed = storage.observeCrashes().first()
            assertEquals(3, observed.size)

            storage.clear()
            assertEquals(0, storage.count())
            assertNull(storage.getLatestCrash())
            assertTrue(storage.getCrashes().isEmpty())
        }

    @Test
    fun testFileCrashStoragePersistence() =
        runTest {
            val fakeFs = FakeFileSystem()
            val fileSystem = FileSystem(directoryPath = "spectra_test", okioFs = fakeFs)
            val storage = FileCrashStorage(fileSystem = fileSystem, maxCrashes = 3)

            val crash1 =
                CrashReport(
                    id = "crash-101",
                    timestamp = 1000L,
                    exceptionClass = "kotlin.UninitializedPropertyAccessException",
                    message = "Lateinit property not initialized",
                    stackTrace = "at com.example.App.start(App.kt:42)",
                    severity = CrashSeverity.FATAL,
                    breadcrumbs =
                        listOf(
                            Breadcrumb(
                                timestamp = 950L,
                                type = BreadcrumbType.USER_ACTION,
                                category = "UI",
                                message = "Clicked login button",
                            ),
                            Breadcrumb(
                                timestamp = 980L,
                                type = BreadcrumbType.NETWORK,
                                category = "POST",
                                message = "/auth/login [200]",
                            ),
                        ),
                    metadata = mapOf("device" to "Pixel 8", "os" to "Android 14"),
                )

            storage.recordCrashSync(crash1)
            assertEquals(1, storage.count())
            val latest = storage.getLatestCrash()
            assertNotNull(latest)
            assertEquals("crash-101", latest.id)
            assertEquals(2, latest.breadcrumbs.size)
            assertEquals("Pixel 8", latest.metadata["device"])

            // Create a second storage instance pointing to same fileSystem to verify disk persistence reload
            val restoredStorage = FileCrashStorage(fileSystem = fileSystem, maxCrashes = 3)
            assertEquals(1, restoredStorage.count())
            val restoredLatest = restoredStorage.getLatestCrash()
            assertNotNull(restoredLatest)
            assertEquals("crash-101", restoredLatest.id)
            assertEquals("Lateinit property not initialized", restoredLatest.message)

            restoredStorage.clear()
            assertEquals(0, restoredStorage.count())
            assertNull(restoredStorage.getLatestCrash())
        }
}
