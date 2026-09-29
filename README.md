# Spectra Logger

[![Version](https://img.shields.io/github/v/release/snooky23/spectra?label=Version&color=brightgreen)](https://github.com/snooky23/spectra/releases)
[![CI](https://github.com/snooky23/spectra/actions/workflows/ci.yml/badge.svg)](https://github.com/snooky23/spectra/actions/workflows/ci.yml)
[![Coverage](https://img.shields.io/badge/coverage-96%25-brightgreen)](https://github.com/snooky23/spectra/actions/workflows/ci.yml)
[![Security](https://github.com/snooky23/spectra/actions/workflows/security.yml/badge.svg)](https://github.com/snooky23/spectra/actions/workflows/security.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-iOS%20%7C%20Android-lightgrey.svg)]()
[![Swift](https://img.shields.io/badge/Swift-5.9+-orange.svg)](https://swift.org)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2+-purple.svg)](https://kotlinlang.org)

A lightweight, on-device logging framework for iOS and Android with a unified, adaptive Compose Multiplatform viewer UI.

## What's Included

| Module | Purpose | Dependency |
|--------|---------|------------|
| **spectra-core** | Logging engine, storage, network interceptors | Required |
| **spectra-ui** | Unified Compose Multiplatform viewer SDK | Optional |

> **Adaptive UI**: The new UI SDK automatically adapts to all screen sizes, providing side-by-side list-detail layouts on tablets and foldables (Android 17+ compliant).

---

## Screenshots & Demos

### In-App Debug Inspector (Compose Multiplatform & Liquid Glass UI)

| Logs View | Network Inspector | Events & Telemetry |
| :---: | :---: | :---: |
| <img src="docs/screenshots/spectra-ui-logs.png" width="260" alt="Spectra Logs Screen" /> | <img src="docs/screenshots/spectra-ui-network.png" width="260" alt="Spectra Network Logs Screen" /> | <img src="docs/screenshots/spectra-ui-events.png" width="260" alt="Spectra Events Screen" /> |
| Filter by level (Debug, Info, Warn, Error), search query, dashboard view | Live HTTP traffic inspection with status codes, latency, payload viewer | Lifecycle, screen dwell tracking, and custom event telemetry |

### Native Example & Playground App

| Generator Actions | Network Traffic Simulation | Event Tracking & Dwell |
| :---: | :---: | :---: |
| <img src="docs/screenshots/ios-example-actions.png" width="260" alt="Example Actions Tab" /> | <img src="docs/screenshots/ios-example-network.png" width="260" alt="Example Network Tab" /> | <img src="docs/screenshots/ios-example-events.png" width="260" alt="Example Events Tab" /> |
| Single & batch logging with levels, metadata, and debug FAB launcher | Simulate GET/POST requests, HTTP errors, timeouts, and batch traffic | Screen views, button taps, lifecycle events, and dwell duration triggers |

---

## Installation

### Android

```kotlin
// build.gradle.kts
dependencies {
    // Core SDK (required)
    implementation("io.github.snooky23:spectra-core:1.0.5")
    
    // Unified UI SDK (optional - adds adaptive log viewer)
    implementation("io.github.snooky23:spectra-ui:1.0.5")
}
```

### iOS (Swift Package Manager)

1. In Xcode: **File → Add Package Dependencies**
2. Enter: `https://github.com/snooky23/spectra`
3. Select products:
   - `SpectraLogger` (Core SDK - required)
   - `SpectraLoggerUI` (Unified UI SDK - optional binary target)

> [!IMPORTANT]
> **iOS ProMotion Configuration**: If using `SpectraLoggerUI`, you must add `CADisableMinimumFrameDurationOnPhone = YES` to your app's `Info.plist` (or Target Build Settings). This is required by the Compose Multiplatform engine for 120Hz display support and consistent rendering performance.

---

## Quick Start

### 1. Log Messages

```kotlin
// Android / KMP
import com.spectra.logger.SpectraLogger

SpectraLogger.d("Auth", "User logged in")
SpectraLogger.w("Network", "Slow response: 2.5s")
```

```swift
// iOS
import SpectraLogger

SpectraLogger.shared.d(tag: "Auth", message: "User logged in", throwable: nil, metadata: [:])
```

### 2. Export Logs (JSON Lines)

```kotlin
// Combine all tracked logs into a single shareable .jsonl file
val exportPath = SpectraLogger.exportLogs()
// On Android: Use FileProvider to share `exportPath` via Intent
// On iOS: Pass `exportPath` directly to UIActivityViewController
```

### 3. Show the Log Viewer (UI SDK)

**Android (Compose):**
```kotlin
import com.spectra.logger.ui.compose.SpectraLoggerScreen

// Wrap your root with the debug FAB overlay
SpectraLoggerFabOverlay(enabled = BuildConfig.DEBUG) {
    MyAppContent()
}
```

**iOS (SwiftUI):**
```swift
import SwiftUI
import SpectraLoggerUI

struct ContentView: View {
    @State private var showLogger = false
    
    var body: some View {
        Button("Open Logs") { showLogger = true }
            .sheet(isPresented: $showLogger) {
                // Wrapper for the Compose Multiplatform UI
                SpectraLoggerView()
            }
    }
}
```

---

## Documentation

- [Master Technical Specification](_bmad-output/refactor-docs/MASTER_SPEC.md) - The central source of truth for architecture and design
- [Developer Onboarding Guide](docs/guides/DEVELOPER_GUIDE.md) - Environment setup and build instructions
- [Adaptive UI Guide](docs/design/UI_DESIGN.md) - Detailed screen layouts and behaviors
- [API Reference](docs/API.md) - Complete API documentation

---

## License

Apache 2.0 - see [LICENSE](LICENSE)
