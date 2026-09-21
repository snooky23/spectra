package com.spectra.logger.feature.crash

import com.spectra.logger.core.utils.IdGenerator
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.crash.interceptor.BreadcrumbRecorder
import com.spectra.logger.feature.crash.interceptor.CrashInterceptor
import com.spectra.logger.feature.crash.interceptor.DefaultBreadcrumbRecorder
import com.spectra.logger.feature.crash.model.CrashReport
import com.spectra.logger.feature.crash.model.CrashSeverity
import com.spectra.logger.feature.crash.storage.CrashStorage
import com.spectra.logger.feature.crash.storage.InMemoryCrashStorage

class SpectraCrashReporter(
    val storage: CrashStorage = InMemoryCrashStorage(),
    val breadcrumbRecorder: BreadcrumbRecorder = DefaultBreadcrumbRecorder(),
    private val interceptorFactory: ((CrashStorage, BreadcrumbRecorder) -> CrashInterceptor)? = null,
) {
    private var interceptor: CrashInterceptor? = null

    fun install() {
        if (interceptor == null) {
            interceptor = interceptorFactory?.invoke(storage, breadcrumbRecorder)
        }
        interceptor?.install()
    }

    fun uninstall() {
        interceptor?.uninstall()
    }

    fun isInstalled(): Boolean = interceptor?.isInstalled() == true

    fun recordNonFatalException(
        throwable: Throwable,
        metadata: Map<String, String> = emptyMap(),
    ) {
        val report =
            CrashReport(
                id = IdGenerator.generate(),
                timestamp = SpectraTime.now().toEpochMilliseconds(),
                exceptionClass = throwable::class.simpleName ?: "Exception",
                message = throwable.message,
                stackTrace = throwable.stackTraceToString(),
                severity = CrashSeverity.NON_FATAL,
                breadcrumbs = breadcrumbRecorder.getBreadcrumbs(),
                metadata = metadata,
            )
        storage.recordCrashSync(report)
    }
}
