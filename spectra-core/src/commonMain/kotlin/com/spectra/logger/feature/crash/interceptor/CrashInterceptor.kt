package com.spectra.logger.feature.crash.interceptor

interface CrashInterceptor {
    fun install()

    fun uninstall()

    fun isInstalled(): Boolean
}
