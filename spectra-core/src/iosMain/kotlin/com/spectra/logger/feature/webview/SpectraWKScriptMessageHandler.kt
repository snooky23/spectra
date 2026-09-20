package com.spectra.logger.feature.webview

import com.spectra.logger.SpectraLogger
import com.spectra.logger.feature.logs.model.LogLevel
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/**
 * Native iOS script message handler that receives JavaScript console messages and errors
 * dispatched from WKWebView instances and forwards them to [SpectraLogger].
 *
 * @property tag Log tag to use for captured messages (default: "WebView")
 */
class SpectraWKScriptMessageHandler(
    private val tag: String = "WebView",
) : NSObject(), WKScriptMessageHandlerProtocol {

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        val body = didReceiveScriptMessage.body
        val bodyMap = body as? Map<*, *>

        val levelStr = bodyMap?.get("level") as? String ?: "log"
        val message = bodyMap?.get("message")?.toString() ?: body.toString()
        val source = bodyMap?.get("source") as? String
        val line = bodyMap?.get("line")?.toString()

        val level = when (levelStr.lowercase()) {
            "debug" -> LogLevel.DEBUG
            "info" -> LogLevel.INFO
            "warn", "warning" -> LogLevel.WARNING
            "error" -> LogLevel.ERROR
            else -> LogLevel.INFO
        }

        val metadata = buildMap {
            put("source", "webview")
            source?.let { put("source_url", it) }
            line?.let { put("line_number", it) }
        }

        SpectraLogger.log(
            level = level,
            tag = tag,
            message = message,
            metadata = metadata,
        )
    }

    companion object {
        const val HANDLER_NAME = "spectra"

        val CONSOLE_BRIDGE_JS = """
            (function() {
                if (window.__spectra_bridge_installed__) return;
                window.__spectra_bridge_installed__ = true;

                function sendToSpectra(level, args, error) {
                    try {
                        var msg = Array.from(args).map(function(arg) {
                            if (typeof arg === 'object') {
                                try { return JSON.stringify(arg); } catch(e) { return String(arg); }
                            }
                            return String(arg);
                        }).join(' ');

                        var payload = {
                            level: level,
                            message: msg,
                            source: window.location.href,
                            line: error && error.line ? error.line : null
                        };

                        if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.spectra) {
                            window.webkit.messageHandlers.spectra.postMessage(payload);
                        }
                    } catch(e) {}
                }

                var originalLog = console.log;
                var originalDebug = console.debug;
                var originalInfo = console.info;
                var originalWarn = console.warn;
                var originalError = console.error;

                console.log = function() { sendToSpectra('log', arguments); originalLog.apply(console, arguments); };
                console.debug = function() { sendToSpectra('debug', arguments); originalDebug.apply(console, arguments); };
                console.info = function() { sendToSpectra('info', arguments); originalInfo.apply(console, arguments); };
                console.warn = function() { sendToSpectra('warn', arguments); originalWarn.apply(console, arguments); };
                console.error = function() { sendToSpectra('error', arguments); originalError.apply(console, arguments); };

                window.addEventListener('error', function(e) {
                    sendToSpectra('error', [e.message || 'Script error'], { line: e.lineno });
                });
            })();
        """.trimIndent()

        /**
         * Creates a WKUserScript configured to inject the Spectra console bridge.
         */
        fun createBridgeScript(): WKUserScript =
            WKUserScript(
                source = CONSOLE_BRIDGE_JS,
                injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                forMainFrameOnly = false,
            )
    }
}

/**
 * Extension function to configure a [WKWebViewConfiguration] with Spectra logging.
 * Call this before initializing your [WKWebView].
 */
fun WKWebViewConfiguration.attachSpectraLogger(tag: String = "WebView") {
    val handler = SpectraWKScriptMessageHandler(tag = tag)
    userContentController.addScriptMessageHandler(handler, name = SpectraWKScriptMessageHandler.HANDLER_NAME)
    userContentController.addUserScript(SpectraWKScriptMessageHandler.createBridgeScript())
}
