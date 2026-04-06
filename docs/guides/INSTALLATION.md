# Installation Guide

Spectra Logger supports two primary installation methods: Swift Package Manager (SPM) for iOS and Gradle for Android.

---

## Android Installation

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Core SDK (required)
    implementation("io.github.snooky23:spectra-core:1.0.5")
    
    // Unified UI SDK (optional - adds adaptive log viewer)
    implementation("io.github.snooky23:spectra-ui:1.0.5")
}
```

---

## iOS Installation (Swift Package Manager)

### Via Xcode (Recommended)

1. **Open your project in Xcode**
2. **File → Add Package Dependencies**
3. **Enter URL:**
   ```
   https://github.com/snooky23/spectra.git
   ```
4. **Select version:** `1.0.5` or "Up to Next Major Version"
5. **Choose products:**
   - ✅ **SpectraLogger** (Core framework - required)
   - ✅ **SpectraLoggerUI** (Unified UI SDK - optional binary target)
6. **Click "Add Package"**

### Via Package.swift

For Swift packages, add to your `Package.swift`:

```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "YourApp",
    platforms: [
        .iOS(.v15)
    ],
    dependencies: [
        .package(
            url: "https://github.com/snooky23/spectra.git",
            from: "1.0.5"
        )
    ],
    targets: [
        .target(
            name: "YourApp",
            dependencies: [
                .product(name: "SpectraLogger", package: "Spectra"),
                .product(name: "SpectraLoggerUI", package: "Spectra")
            ]
        )
    ]
)
```

---

## Platform Requirements

| Platform | Minimum Version | Note |
|----------|-----------------|------|
| **Android** | API 24 (7.0) | Supports up to Android 17 (API 35+) |
| **iOS** | iOS 15.0+ | Required for Compose Multiplatform UI |

---

## Post-Installation Setup

### 1. iOS Integration Bridge

Since the UI SDK is built with Compose Multiplatform, you need to wrap it in a SwiftUI view. Create a file named `SpectraLoggerView.swift` in your project:

```swift
import SwiftUI
import UIKit
import SpectraLogger
import SpectraLoggerUI

struct SpectraLoggerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Initialize the UI bridge
        SpectraUI.shared.init()
        
        // Return the shared view controller
        return SpectraLoggerViewControllerKt.SpectraLoggerViewController(onDismiss: {
            SpectraUIManager.shared.dismissScreen()
        })
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### 2. Initialization

**Android (Application class):**
```kotlin
SpectraLogger.configure {
    // optional configuration
}
```

**iOS (App struct):**
```swift
import SpectraLogger

@main
struct YourApp: App {
    init() {
        SpectraLogger.shared.configure { _ in }
    }
    // ...
}
```

---

## Troubleshooting

### "No such module SpectraLoggerUI"
1. In Xcode: **Product → Clean Build Folder**.
2. **File → Packages → Reset Package Caches**.
3. Ensure your deployment target is at least **iOS 15.0**.

### Checksum Mismatch
If you are updating to a new version and see a checksum mismatch, please reset your package cache or delete the `DerivedData` folder.
