package com.spectra.logger.feature.crash.interceptor

import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import com.spectra.logger.feature.crash.storage.CrashStorage
import kotlinx.atomicfu.atomic
import kotlin.native.setUnhandledExceptionHook

class SpectraIosCrashHandler(
    private val crashStorage: CrashStorage,
    private val breadcrumbRecorder: BreadcrumbRecorder,
    private val deviceInfoProvider: (() -> Map<String, String>)? = null,
    private val onCrashRecorded: ((CrashReport) -> Unit)? = null,
) : CrashInterceptor {
    private val isInstalledAtomic = atomic(false)

    override fun install() {
        if (isInstalledAtomic.compareAndSet(expect = false, update = true)) {
            setUnhandledExceptionHook { throwable ->
                try {
                    val report =
                        CrashReport(
                            id = IdGenerator.generate(),
                            timestamp = SpectraTime.now().toEpochMilliseconds(),
                            exceptionClass = throwable::class.qualifiedName ?: throwable::class.simpleName ?: "Throwable",
                            message = throwable.message,
                            stackTrace = throwable.stackTraceToString(),
                            threadName = "main",
                            severity = CrashSeverity.FATAL,
                            breadcrumbs = breadcrumbRecorder.getBreadcrumbs(),
                            metadata = deviceInfoProvider?.invoke() ?: emptyMap(),
                        )
                    crashStorage.recordCrashSync(report)
                    onCrashRecorded?.invoke(report)
                } catch (_: Throwable) {
                    // Suppress error inside crash hook
                }
            }
        }
    }

    override fun uninstall() {
        if (isInstalledAtomic.compareAndSet(expect = true, update = false)) {
            setUnhandledExceptionHook { _ -> }
        }
    }

    override fun isInstalled(): Boolean = isInstalledAtomic.value
}
