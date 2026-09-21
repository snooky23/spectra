package com.spectra.logger.feature.crash.interceptor

import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import com.spectra.logger.feature.crash.storage.CrashStorage
import kotlinx.atomicfu.atomic

class SpectraCrashHandler(
    private val crashStorage: CrashStorage,
    private val breadcrumbRecorder: BreadcrumbRecorder,
    private val deviceInfoProvider: (() -> Map<String, String>)? = null,
    private val onCrashRecorded: ((CrashReport) -> Unit)? = null,
) : Thread.UncaughtExceptionHandler, CrashInterceptor {
    private val defaultHandlerAtomic = atomic<Thread.UncaughtExceptionHandler?>(null)
    private val isInstalledAtomic = atomic(false)

    override fun install() {
        if (isInstalledAtomic.compareAndSet(expect = false, update = true)) {
            val existing = Thread.getDefaultUncaughtExceptionHandler()
            defaultHandlerAtomic.value = existing
            Thread.setDefaultUncaughtExceptionHandler(this)
        }
    }

    override fun uninstall() {
        if (isInstalledAtomic.compareAndSet(expect = true, update = false)) {
            val previous = defaultHandlerAtomic.value
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
    }

    override fun isInstalled(): Boolean = isInstalledAtomic.value

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable,
    ) {
        try {
            val report =
                CrashReport(
                    id = IdGenerator.generate(),
                    timestamp = SpectraTime.now().toEpochMilliseconds(),
                    exceptionClass = throwable::class.qualifiedName ?: throwable::class.simpleName ?: "Throwable",
                    message = throwable.message,
                    stackTrace = throwable.stackTraceToString(),
                    threadName = thread.name,
                    severity = CrashSeverity.FATAL,
                    breadcrumbs = breadcrumbRecorder.getBreadcrumbs(),
                    metadata = deviceInfoProvider?.invoke() ?: emptyMap(),
                )
            crashStorage.recordCrashSync(report)
            onCrashRecorded?.invoke(report)
        } catch (_: Throwable) {
            // Avoid failing inside uncaught exception handler
        } finally {
            defaultHandlerAtomic.value?.uncaughtException(thread, throwable)
        }
    }
}
