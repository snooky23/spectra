package com.spectra.logger.feature.crash.interceptor

import com.spectra.logger.feature.crash.model.Breadcrumb
import com.spectra.logger.feature.crash.model.BreadcrumbType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BreadcrumbRecorderTest {
    @Test
    fun testBreadcrumbRecorderEvictionAndOrdering() {
        val recorder = DefaultBreadcrumbRecorder(maxCapacity = 3)
        assertTrue(recorder.getBreadcrumbs().isEmpty())

        val bc1 = Breadcrumb(timestamp = 10L, type = BreadcrumbType.LOG, category = "Auth", message = "Attempting login")
        val bc2 = Breadcrumb(timestamp = 20L, type = BreadcrumbType.NETWORK, category = "POST", message = "/login")
        val bc3 = Breadcrumb(timestamp = 30L, type = BreadcrumbType.EVENT, category = "ScreenView", message = "Dashboard")
        val bc4 = Breadcrumb(timestamp = 40L, type = BreadcrumbType.USER_ACTION, category = "UI", message = "Clicked button")

        recorder.record(bc1)
        recorder.record(bc2)
        recorder.record(bc3)
        assertEquals(3, recorder.getBreadcrumbs().size)
        assertEquals(listOf(10L, 20L, 30L), recorder.getBreadcrumbs().map { it.timestamp })

        recorder.record(bc4) // Should evict bc1 (timestamp 10L)
        assertEquals(3, recorder.getBreadcrumbs().size)
        assertEquals(listOf(20L, 30L, 40L), recorder.getBreadcrumbs().map { it.timestamp })

        recorder.clear()
        assertTrue(recorder.getBreadcrumbs().isEmpty())
    }
}
