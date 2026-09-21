package com.spectra.logger.feature.crash

import com.spectra.logger.feature.crash.interceptor.SpectraIosCrashHandler
import com.spectra.logger.feature.crash.model.CrashReport

fun SpectraCrashReporter.installIosHandler(
    deviceInfoProvider: (() -> Map<String, String>)? = null,
    onCrashRecorded: ((CrashReport) -> Unit)? = null,
): SpectraIosCrashHandler {
    val handler =
        SpectraIosCrashHandler(
            crashStorage = storage,
            breadcrumbRecorder = breadcrumbRecorder,
            deviceInfoProvider = deviceInfoProvider,
            onCrashRecorded = onCrashRecorded,
        )
    handler.install()
    return handler
}
