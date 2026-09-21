package com.spectra.logger.feature.crash

import com.spectra.logger.feature.crash.interceptor.SpectraCrashHandler
import com.spectra.logger.feature.crash.model.CrashReport

fun SpectraCrashReporter.installAndroidHandler(
    deviceInfoProvider: (() -> Map<String, String>)? = null,
    onCrashRecorded: ((CrashReport) -> Unit)? = null,
): SpectraCrashHandler {
    val handler =
        SpectraCrashHandler(
            crashStorage = storage,
            breadcrumbRecorder = breadcrumbRecorder,
            deviceInfoProvider = deviceInfoProvider,
            onCrashRecorded = onCrashRecorded,
        )
    handler.install()
    return handler
}
