package com.spectra.logger.feature.logs.export

import com.spectra.logger.feature.crash.model.Breadcrumb
import com.spectra.logger.feature.crash.model.BreadcrumbType
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import kotlin.test.Test
import kotlin.test.assertTrue

class CrashExporterTest {
    @Test
    fun testCrashReportExportFormats() {
        val report =
            CrashReport(
                id = "test-crash-uuid",
                timestamp = 1718900000000L,
                exceptionClass = "java.lang.NullPointerException",
                message = "Object reference is null",
                stackTrace = "java.lang.NullPointerException: Object reference is null\n\tat com.test.Main.run(Main.kt:12)",
                threadName = "main",
                severity = CrashSeverity.FATAL,
                breadcrumbs =
                    listOf(
                        Breadcrumb(
                            timestamp = 1718899990000L,
                            type = BreadcrumbType.LOG,
                            category = "App",
                            message = "App started",
                        ),
                        Breadcrumb(
                            timestamp = 1718899995000L,
                            type = BreadcrumbType.NETWORK,
                            category = "GET",
                            message = "/api/user [200]",
                        ),
                    ),
                metadata = mapOf("manufacturer" to "Google", "model" to "Pixel 8"),
            )

        val text = LogExporter.exportCrashAsText(report)
        assertTrue(text.contains("=== Spectra Crash Report ==="))
        assertTrue(text.contains("java.lang.NullPointerException"))
        assertTrue(text.contains("Object reference is null"))
        assertTrue(text.contains("App started"))
        assertTrue(text.contains("manufacturer: Google"))

        val json = LogExporter.exportCrashAsJson(report)
        assertTrue(json.contains("\"id\": \"test-crash-uuid\""))
        assertTrue(json.contains("\"exceptionClass\": \"java.lang.NullPointerException\""))
        assertTrue(json.contains("\"severity\": \"FATAL\""))

        val md = LogExporter.exportCrashAsMarkdown(report)
        assertTrue(md.contains("# 💥 Spectra Crash Report"))
        assertTrue(md.contains("**Exception**: `java.lang.NullPointerException`"))
        assertTrue(md.contains("## 📋 Stack Trace"))
        assertTrue(md.contains("## 🍞 Breadcrumbs (2)"))
        assertTrue(md.contains("## 📱 Device Context & Metadata"))
    }
}
