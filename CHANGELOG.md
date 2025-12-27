# Changelog

All notable changes to Spectra Logger will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-12-27

### 🎉 Initial Release

First public release of Spectra Logger - a lightweight, on-device logging framework for iOS and Android.

### ✨ Features

#### Core Logging Engine
- **Multi-level logging** - Support for verbose, debug, info, warning, and error log levels
- **Categorized logs** - Organize logs by custom categories for better filtering
- **Thread-safe** - Safe for use in multi-threaded environments
- **Lightweight** - Minimal memory footprint and performance impact

#### Network Interception
- **Automatic HTTP/HTTPS logging** - Capture all network requests and responses
- **Request details** - Method, URL, headers, body, timing
- **Response details** - Status code, headers, body, duration
- **cURL export** - Export requests as cURL commands for debugging

#### Storage
- **In-memory storage** - Fast access to recent logs
- **File persistence** - Optional disk storage for crash investigation
- **Automatic pruning** - Smart cleanup to prevent storage bloat
- **Export capabilities** - JSON, text, and CSV export formats

#### iOS UI (SpectraUI)
- **Native SwiftUI** - Beautiful, native iOS interface
- **Real-time streaming** - Live log updates as they happen
- **Advanced filtering** - Filter by level, category, time range, and search
- **Network inspector** - Detailed view of network requests with JSON highlighting
- **Shake to show** - Debug logs with a simple shake gesture

#### Android UI (spectra-ui-android)
- **Jetpack Compose** - Modern Material 3 design
- **Dark mode support** - Follows system theme
- **Filter chips** - Quick filtering by log level
- **Network logs view** - Dedicated screen for network requests
- **Share functionality** - Easy log sharing

### 📦 Installation

#### iOS (Swift Package Manager)
```swift
dependencies: [
    .package(url: "https://github.com/snooky23/spectra", from: "0.1.0")
]
```

#### Android (Gradle)
```kotlin
implementation("io.github.snooky23:spectra-core:0.1.0")
```

### 🔧 Requirements

| Platform | Minimum Version |
|----------|-----------------|
| iOS      | 15.0+           |
| Android  | API 24+ (7.0)   |
| Kotlin   | 1.9+            |
| Swift    | 5.9+            |

### 📝 License

Apache License 2.0

---

[0.1.0]: https://github.com/snooky23/spectra/releases/tag/v0.1.0
