package com.spectra.logger.feature.network.interceptor

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class SpectraURLSessionDelegate : NSObject(), NSURLSessionDataDelegateProtocol {
    private val protocols = mutableMapOf<NSURLSessionTask, SpectraURLProtocol>()

    fun register(task: NSURLSessionTask, protocol: SpectraURLProtocol) {
        // Need to synchronize? Kotlin Native memory model usually requires atomic or main thread.
        // NSURLProtocol methods are typically called on a background thread.
    }
}
