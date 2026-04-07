# Spectra Logger - Usage Guide

Step-by-step guide for integrating and using Spectra Logger in your mobile applications.

## Table of Contents

1. [Installation](#installation)
2. [Quick Start](#quick-start)
3. [Adaptive UI Integration](#adaptive-ui-integration)
4. [Advanced Features](#advanced-features)

---

## Installation

### Android

Add the unified KMP UI and Core SDKs to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.snooky23:spectra-core:1.0.4")
    implementation("io.github.snooky23:spectra-ui:1.0.4")
}
```

### iOS (Swift Package Manager)

Add the Spectra Logger repository to your Xcode project and select both `SpectraLogger` and `SpectraLoggerUI`.

---

## Quick Start

### 1. Initialize the Logger

**Android (Application class):**
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SpectraLogger.configure {
            minLogLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.INFO
        }
    }
}
```

**iOS (Swift):**
```swift
@main
struct MyApp: App {
    init() {
        SpectraLogger.shared.configure { _ in }
    }
}
```

### 2. Start Logging

```kotlin
// Common code or Android
SpectraLogger.i("App", "Application started")
SpectraLogger.e("Auth", "Login failed", throwable = exception)
```

```swift
// iOS
SpectraLogger.shared.i(tag: "App", message: "iOS app initialized")
```

---

## Adaptive UI Integration

Spectra Logger features a unified, adaptive UI built with Compose Multiplatform. It automatically handles orientation changes and adapts to large screens (tablets/foldables) by providing side-by-side list-detail views.

### Android: Draggable FAB Overlay

The easiest way to integrate the logger on Android is by wrapping your root Composable with the `SpectraLoggerFabOverlay`.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpectraLoggerFabOverlay(enabled = BuildConfig.DEBUG) {
                MyMainAppContent()
            }
        }
    }
}
```

### iOS: SwiftUI Wrapper

On iOS, you wrap the SDK's view controller in a `UIViewControllerRepresentable`.

```swift
// SpectraLoggerView.swift
struct SpectraLoggerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        SpectraUI.shared.init()
        return SpectraLoggerViewControllerKt.SpectraLoggerViewController(onDismiss: {
            SpectraUIManager.shared.dismissScreen()
        })
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// In your SwiftUI code
.sheet(isPresented: $showLogs) {
    SpectraLoggerView()
}
```

---

## Advanced Features

### Network Request Logging

**Android (OkHttp):**
```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(SpectraNetworkInterceptor(SpectraLogger.networkStorage))
    .build()
```

**iOS (URLSession):**
```swift
SpectraURLSessionLogger.logRequest(...)
```

### Smart Share

The log viewer includes a "Smart Share" feature that allows you to choose between sharing only your currently filtered logs or the entire log history. On both platforms, this utilizes the native platform share sheet.

### Large Screen Support (Android 17)

The UI SDK is optimized for Android 17 desktop mode and multi-window environments. It uses a `NavigationRail` or `NavigationDrawer` when space is available, ensuring your debug tools are always accessible and usable on any device size.
