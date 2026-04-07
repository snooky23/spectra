# Cross-Platform Integration Guide

Spectra Logger acts as a unified debugging surface for Kotlin Multiplatform mobile apps. While the core business logic is pure Kotlin, integrating the network interception layer and the UI hooks relies on platform-specific bridging.

This document walks you through the exact steps needed to fully operationalize Spectra globally.

---

## 1. Network Interceptors

Merely enabling `enableNetworkLogging` in the configuration is not enough. The native networking clients must route their traffic through Spectra's interceptors.

### Android: OkHttp Client
If your networking implementation uses Ktor backed by OkHttp—or you rely on Retrofit/OkHttp directly on the native side—you **must** append the `SpectraNetworkInterceptor` to the OkHttpClient builder.

```kotlin
import com.spectra.logger.network.SpectraNetworkInterceptor
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(SpectraNetworkInterceptor())
    .build()
```
Network logging will now automatically collect all queries passing through this OkHttp instance.

### iOS: URLProtocol
For native iOS `URLSession` traffic, you must globally register the Spectra protocol class.

```swift
import SpectraLogger

// Invoke this globally in your App `init()` or AppDelegate
SpectraLogger.shared.registerURLProtocol()
```
This forces all base `URLSession.shared` tasks to be captured by Spectra. For custom `URLSessionConfiguration` objects, ensure you manually add `SpectraURLProtocol` to the `protocolClasses` array.

---

## 2. Debug Menu Access

The fastest way to test your logs is to provide developers/QA with a direct entry point to the UI.

### Android: Global FAB Overlay

In Jetpack Compose applications, wrap your root element in `SpectraLoggerFabOverlay` to gain a draggable button that opens the Logs Viewer.

```kotlin
// Android MainActivity.kt
import com.spectra.logger.ui.compose.SpectraLoggerFabOverlay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpectraLoggerFabOverlay(enabled = BuildConfig.DEBUG) {
                MainAppScreen()
            }
        }
    }
}
```

### iOS: SwiftUI Sheet

For iOS applications, you can present the `SpectraLoggerView` (which you create using the `UIViewControllerRepresentable` bridge) as a standard sheet.

```swift
// iOS YourView.swift
import SwiftUI
import SpectraLoggerUI

struct MainAppView: View {
    @State private var showLogs = false
    
    var body: some View {
        Button("Open Logs") {
            showLogs = true
        }
        .sheet(isPresented: $showLogs) {
            SpectraLoggerView()
        }
    }
}
```

---

## 3. Configuration Warning

Always invoke configuration strictly once during app bootstrapping (e.g. `Application.onCreate` or `@main App.init()`).

**Android:**
```kotlin
SpectraLogger.configure { config ->
    config.enabledFeatures.enableNetworkLogging = true
}
```

**iOS:**
```swift
SpectraLogger.shared.configure { config in
    config.enabledFeatures.enableNetworkLogging = true
}
```
