package com.spectra.logger

import com.spectra.logger.domain.model.LogLevel

/**
 * Mock Spectra Logger for the example app.
 * This is a minimal implementation for demonstration purposes.
 */
object SpectraLogger {
    var configuration: LoggerConfiguration = LoggerConfiguration()

    fun configure(block: LoggerConfiguration.() -> Unit) {
        configuration = LoggerConfiguration().apply(block)
    }

    fun getVersion(): String = "1.0.0"

    fun v(tag: String, message: String, metadata: Map<String, Any>? = null) {
        println("[VERBOSE] $tag: $message")
    }

    fun d(tag: String, message: String, metadata: Map<String, Any>? = null) {
        println("[DEBUG] $tag: $message")
    }

    fun i(tag: String, message: String, metadata: Map<String, Any>? = null) {
        println("[INFO] $tag: $message")
    }

    fun w(tag: String, message: String, metadata: Map<String, Any>? = null) {
        println("[WARNING] $tag: $message")
    }

    fun e(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any>? = null) {
        println("[ERROR] $tag: $message")
        throwable?.printStackTrace()
    }

    fun f(tag: String, message: String, metadata: Map<String, Any>? = null) {
        println("[FATAL] $tag: $message")
    }
}

class LoggerConfiguration {
    var minLogLevel: LogLevel = LogLevel.VERBOSE

    fun logStorage(block: StorageConfig.() -> Unit) {
        // No-op for example
    }

    fun networkStorage(block: StorageConfig.() -> Unit) {
        // No-op for example
    }
}

class StorageConfig {
    var maxCapacity: Int = 0
}
