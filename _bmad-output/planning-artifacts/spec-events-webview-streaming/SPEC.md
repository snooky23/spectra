# Specification: Events Telemetry, WebView Logging & Remote Log Streaming

## Overview

This specification formalizes the next major evolutionary phase for Spectra Logger, structured into three coordinated milestones:
1. **Phase 12 — Events Tab (Screen Views & User Analytics)**: In-app telemetry tracking screen navigation, interaction events, durations, and state transitions, rendered natively across Android and iOS via Compose Multiplatform.
2. **Phase 13 — WebView & JavaScript Logging**: Direct bridge capturing `console.log/warn/error` and unhandled exceptions from Android `android.webkit.WebView` and iOS `WebKit.WKWebView`.
3. **Phase 14 — Remote Log Streaming (WebSocket & Local Wi-Fi Companion)**: Real-time bi-directional streaming of mobile logs, network requests, events, and screen states to a desktop browser over local Wi-Fi, paired via camera QR-code scanning, protected by browser-side authorization prompts, supporting historical catch-up sync and live incremental updates.

---

## Capabilities & Requirements

### CAP-1: Events Telemetry System (`spectra-core` & `spectra-ui`)
- **Event Data Model**: `EventLogEntry` containing `id`, `timestamp`, `eventType` (`SCREEN_VIEW`, `USER_ACTION`, `LIFECYCLE`, `CUSTOM`), `name`, `parameters: Map<String, String>`, `durationMs: Long?`, and `sourceType`.
- **Public API in `SpectraLogger`**:
  - `SpectraLogger.event(name: String, parameters: Map<String, String> = emptyMap())`
  - `SpectraLogger.screenStart(screenName: String, parameters: Map<String, String> = emptyMap())`
  - `SpectraLogger.screenEnd(screenName: String)` (calculates duration automatically)
- **Storage & Query**: `EventLogStorage` and `InMemoryEventLogStorage` supporting filtering by type, search, count, and export.
- **Unified UI Screen**:
  - New **"Events"** tab in `SpectraNavigationSuiteScaffold` between Network and Settings.
  - Interactive timeline view with screen duration pills and parameter inspection pane.
  - Adaptive layout adhering to `UI_DESIGN.md` (compact push/pop stack on phone, dual-pane master/detail on tablets/desktop).

### CAP-2: WebView / JavaScript Console Bridge (`spectra-core`)
- **Android Interceptor**: `SpectraWebChromeClient` extending `WebChromeClient`, capturing `ConsoleMessage` (message, source ID, line number, level) and routing to `SpectraLogger`.
- **iOS Interceptor**: `SpectraScriptMessageHandler` implementing `WKScriptMessageHandler` with injected JavaScript snippet overriding `window.console` (`log`, `debug`, `info`, `warn`, `error`) and forwarding payloads via `window.webkit.messageHandlers.spectra.postMessage(...)`.
- **Log Model Harmonization**: Log entries tagged with `[WebView]` tag, source URL, line number, and level mapping (`DEBUG`, `INFO`, `WARNING`, `ERROR`).
- **UI Filtering**: Quick-filter chip in Logs tab and dedicated WebView filter options.

### CAP-3: Remote Log Streaming over Local Wi-Fi (`spectra-core` & Desktop Web Companion)
- **Local Wi-Fi Discovery & Pairing**:
  - Desktop companion browser serves a local dashboard on port `9292` (or configurable).
  - Browser displays a secure QR Code containing `ws://<desktop-ip>:9292/spectra-ws?token=<session_secret>`.
  - Mobile scans QR code (Android CameraX / Google Code Scanner, iOS `AVFoundation`).
- **Browser Authorization Gatekeeper**:
  - Mobile initiates WebSocket handshake transmitting device metadata (`deviceName`, `os`, `osVersion`, `appVersion`, `ip`).
  - Browser displays modal: *"Device [Device Name] ([OS]) wants to stream logs. Allow connection?"*.
  - Until the desktop user explicitly clicks **"Allow"**, no log streaming or telemetry packets are accepted.
- **Two-Phase Synchronization Protocol**:
  - **Phase 1 (Historical Batch Sync)**: Upon authorization, mobile queries buffered `LogStorage`, `NetworkLogStorage`, and `EventLogStorage`, sending compressed chunks (100–200 entries/batch) to quickly populate the browser.
  - **Phase 2 (Live Incremental Streaming)**: Mobile subscribes to Kotlin coroutine `Flow` streams (`observe()`) and pushes incoming entries with sub-10ms latency over local Wi-Fi.
- **Big-Screen Browser Dashboard**:
  - Reuses Spectra's Adaptive Dual-Pane layout (`UI_DESIGN.md`).
  - Full observability: Logs tab, Network request/response inspector, Events timeline, and WebView console.
