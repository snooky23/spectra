# Cross-Platform Integration Guide

Spectra Logger acts as a unified debugging surface for Kotlin Multiplatform mobile apps. While the `shared` business logic is pure Kotlin, integrating the network interception layer and the UI hooks relies heavily on the specific native platforms.

This document walks you through the exact steps needed to fully operationalize Spectra globally.

---

## 1. Network Interceptors

Merely enabling `enableNetworkLogging` in the configuration is not enough. The native networking clients must route their traffic through Spectra's interceptors.

### Android: OkHttp Client
If your `shared` KMP networking implementation uses Ktor backed by OkHttp—or you rely on Retrofit/OkHttp directly on the native side—you **must** append the `SpectraNetworkInterceptor` to the OkHttpClient builder.

```kotlin
import com.spectra.logger.network.SpectraNetworkInterceptor
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(SpectraNetworkInterceptor())
    .build()
```
Network logging will now automatically collect all queries passing through this OkHttp instance unless blocked by `networkIgnoredDomains`.

### iOS: URLProtocol
For native iOS `URLSession` traffic, you must globally register the Spectra protocol class before your session instantiates.

```swift
import shared

// Invoke this globally in your AppDelegate or equivalent App `init()`
SpectraLogger.registerURLProtocol()
```
This forces all base `URLSession.shared` tasks to bounce through Spectra's listener. If you configure custom `URLSessionConfiguration` objects (such as for Alamofire), ensure you manually inject the protocol class into the configuration list.

---

## 2. Debug Menu Access

The fastest way to test your logs is to provide developers/QA with a direct entry point to the UI from anywhere in the mobile app. We've built mechanisms natively for Android and iOS to streamline this.

### Android: Global FAB Overlay

In modern Jetpack Compose applications, your app hierarchy typically starts at a `setContent { ... }` block inside your `MainActivity`.

Wrap that root element in `SpectraLoggerFabOverlay` to gain a draggable overlay button that opens the Logs Viewer.

```kotlin
// Android MainActivity.kt
import com.spectra.logger.ui.compose.SpectraLoggerFabOverlay
import com.spectra.logger.ui.SpectraUIManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpectraLoggerFabOverlay(enabled = BuildConfig.DEBUG) {
                // Your Application's Root Theme
                MainAppScreen()
            }
        }
    }
}
```

By toggling the `enabled` boolean based on `BuildConfig.DEBUG`, you guarantee the button is securely stripped from production builds. 

### iOS: Hardware Shake Gesture

For iOS SwiftUI applications, forcing a permanent FAB can clog the UI. Instead, we intercept the hardware physics `motionShake` event.

Add the `.onShakeToRevealSpectraLogger()` modifier directly on your `WindowGroup`'s primary `View`.

```swift
// iOS YourApp.swift
import SwiftUI
import shared
import SpectraLoggerUI // Contains the Shake Modifier Extension

@main
struct SpectraExampleApp: App {
    var body: some Scene {
        WindowGroup {
            MainAppView()
                .onShakeToRevealSpectraLogger(enabled: true) // Set to false in Prod!
        }
    }
}
```
If you are running the project inside the Xcode iOS Simulator, use `Cmd + Control + Z` to simulate a physical hardware shake. The Spectra Logger modal sheet will slide up containing all collected telemetry.

---

## 3. Dependency Injection Warning

Remember that the `SpectraLogger` state relies on an internal KMP singleton. Repeatedly calling `SpectraLogger.configure { ... }` natively during navigation lifecycle events (like `onResume` or `onAppear`) will continually tear down and reinitialize the internal storage flows.

*Always invoke configuration strictly once during absolute app bootstrapping (e.g. `Application.onCreate` or `@main App.init()`).*
