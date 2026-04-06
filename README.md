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

### 2. Show the Log Viewer (UI SDK)

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

- [KMP UI Migration Spec](docs/design/KMP_UI_ADAPTIVE_SPEC.md) - Architectural details of the unified UI
- [Adaptive UI Guide](docs/design/UI_DESIGN.md) - Screen layouts and behaviors
- [API Reference](docs/API.md) - Complete API documentation

---

## License

Apache 2.0 - see [LICENSE](LICENSE)
