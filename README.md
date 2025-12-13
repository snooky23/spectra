# Spectra Logger

[![Build](https://github.com/snooky23/spectra/actions/workflows/build.yml/badge.svg)](https://github.com/snooky23/spectra/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A lightweight, on-device logging framework for iOS and Android with a built-in viewer UI.

## What's Included

| Module | Purpose | Dependency |
|--------|---------|------------|
| **spectra-core** | Logging engine, storage, network interceptors | Required |
| **spectra-ui-android** | Compose UI viewer for Android | Optional |
| **spectra-ui-ios** | SwiftUI viewer for iOS | Optional |

> **Core-only usage**: Use `spectra-core` for headless logging (CI/tests, background services). Add the UI module when you need an on-device log viewer.

---

## Installation

### Android

```kotlin
// build.gradle.kts
dependencies {
    // Core SDK (required)
    implementation("com.spectra.logger:spectra-core:0.0.1")
    
    // UI SDK (optional - adds log viewer)
    implementation("com.spectra.logger:spectra-ui-android:0.0.1")
}
```

### iOS (Swift Package Manager)

1. In Xcode: **File → Add Package Dependencies**
2. Enter: `https://github.com/snooky23/spectra`
3. Select products:
   - `SpectraLogger` (Core SDK - required)
   - `SpectraUI` (UI SDK - optional)

Or in `Package.swift`:
```swift
dependencies: [
    .package(url: "https://github.com/snooky23/spectra", from: "0.0.1")
]
```

---

## Quick Start

### 1. Log Messages

```kotlin
// Android / KMP
import com.spectra.logger.SpectraLogger

SpectraLogger.d("Auth", "User logged in")
SpectraLogger.w("Network", "Slow response: 2.5s")
SpectraLogger.e("Payment", "Transaction failed", metadata = mapOf("orderId" to "12345"))
```

```swift
// iOS
import SpectraLogger

SpectraLogger.shared.d(tag: "Auth", message: "User logged in", throwable: nil, metadata: [:])
SpectraLogger.shared.e(tag: "Payment", message: "Transaction failed", throwable: nil, metadata: ["orderId": "12345"])
```

### 2. Log Network Requests

**Android (OkHttp):**
```kotlin
import com.spectra.logger.network.SpectraNetworkInterceptor

val client = OkHttpClient.Builder()
    .addInterceptor(SpectraNetworkInterceptor(SpectraLogger.networkStorage))
    .build()
```

**iOS (URLSession):**
```swift
import SpectraLogger

let storage = SpectraLogger.shared.networkStorage
SpectraURLSessionLogger.logRequest(
    url: url,
    method: "GET",
    response: response,
    data: data,
    error: error,
    duration: durationMs,
    storage: storage
)
```

### 3. Show the Log Viewer (UI SDK)

**Android:**
```kotlin
import com.spectra.logger.ui.compose.SpectraLoggerScreen

// In your Composable
var showLogger by remember { mutableStateOf(false) }

Button(onClick = { showLogger = true }) {
    Text("Open Logs")
}

if (showLogger) {
    SpectraLoggerScreen(onDismiss = { showLogger = false })
}
```

**iOS:**
```swift
import SpectraUI

struct ContentView: View {
    @State private var showLogger = false
    
    var body: some View {
        Button("Open Logs") { showLogger = true }
            .sheet(isPresented: $showLogger) {
                SpectraLoggerView()
            }
    }
}
```

---

## Log Levels

| Level | Function | Use Case |
|-------|----------|----------|
| VERBOSE | `v()` | Detailed tracing |
| DEBUG | `d()` | Development info |
| INFO | `i()` | General events |
| WARNING | `w()` | Potential issues |
| ERROR | `e()` | Failures |
| FATAL | `f()` | Critical errors |

---

## Configuration (Optional)

```kotlin
SpectraLogger.configure {
    minLogLevel = LogLevel.DEBUG
    logStorage { maxCapacity = 20_000 }
    networkStorage { maxCapacity = 2_000 }
}
```

---

## Example Apps

See working examples in:
- [`examples/android-native`](examples/android-native) - Native Android with Compose
- [`examples/ios-native`](examples/ios-native) - Native iOS with SwiftUI

---

## Documentation

- [Changelog](CHANGELOG.md) - Release notes and version history
- [UI Design](docs/design/UI_DESIGN.md) - Log viewer screens and components
- [API Reference](docs/API.md) - Complete API documentation
- [Future Enhancements](docs/design/FUTURE_ENHANCEMENTS.md) - Roadmap

---

## Building from Source

```bash
# Clone
git clone https://github.com/snooky23/spectra.git
cd spectra

# Build Android
./gradlew build

# Build iOS XCFramework
./scripts/build/build-xcframework.sh
```

---

## License

Apache 2.0 - see [LICENSE](LICENSE)
