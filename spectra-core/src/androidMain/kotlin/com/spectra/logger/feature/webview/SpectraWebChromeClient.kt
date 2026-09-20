package com.spectra.logger.feature.webview

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.logs.model.LogLevel

/**
 * WebChromeClient that captures JavaScript console logs and errors from Android WebViews
 * and forwards them to [SpectraLogger].
 *
 * Employs the Decorator pattern to safely wrap any developer-provided [delegate] client.
 *
 * @property delegate Optional existing [WebChromeClient] to forward events to
 * @property tag Log tag to use for captured console messages (default: "WebView")
 */
open class SpectraWebChromeClient(
    private val delegate: WebChromeClient? = null,
    private val tag: String = "WebView",
    private val logger: (LogLevel, String, String, Throwable?, Map<String, String>?) -> Unit = { level, tag, msg, thr, meta ->
        SpectraLogger.log(level = level, tag = tag, message = msg, throwable = thr, metadata = meta)
    },
) : WebChromeClient() {
    internal fun mapLevel(messageLevel: ConsoleMessage.MessageLevel?): LogLevel =
        when (messageLevel) {
            ConsoleMessage.MessageLevel.DEBUG -> LogLevel.DEBUG
            ConsoleMessage.MessageLevel.ERROR -> LogLevel.ERROR
            ConsoleMessage.MessageLevel.LOG -> LogLevel.INFO
            ConsoleMessage.MessageLevel.TIP -> LogLevel.DEBUG
            ConsoleMessage.MessageLevel.WARNING -> LogLevel.WARNING
            null -> LogLevel.INFO
        }

    internal fun handleConsoleLog(
        level: LogLevel,
        message: String,
        sourceId: String? = null,
        lineNumber: Int? = null,
    ) {
        val metadata =
            buildMap {
                put("source", "webview")
                sourceId?.let { put("source_url", it) }
                lineNumber?.let { put("line_number", it.toString()) }
            }

        logger(level, tag, message, null, metadata)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (consoleMessage != null) {
            val level = mapLevel(consoleMessage.messageLevel())
            handleConsoleLog(
                level = level,
                message = consoleMessage.message().orEmpty(),
                sourceId = consoleMessage.sourceId(),
                lineNumber = consoleMessage.lineNumber(),
            )
        }

        return delegate?.onConsoleMessage(consoleMessage) ?: super.onConsoleMessage(consoleMessage)
    }

    override fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    ) {
        delegate?.onProgressChanged(view, newProgress) ?: super.onProgressChanged(view, newProgress)
    }

    override fun onReceivedTitle(
        view: WebView?,
        title: String?,
    ) {
        delegate?.onReceivedTitle(view, title) ?: super.onReceivedTitle(view, title)
    }
}

/**
 * Attaches Spectra logging to this [WebView].
 *
 * @param existingClient Optional existing [WebChromeClient] to wrap and preserve
 * @param tag Custom log tag (default: "WebView")
 * @return The configured [SpectraWebChromeClient]
 */
fun WebView.attachSpectraLogger(
    existingClient: WebChromeClient? = null,
    tag: String = "WebView",
): SpectraWebChromeClient {
    val client = SpectraWebChromeClient(delegate = existingClient, tag = tag)
    this.webChromeClient = client
    return client
}
