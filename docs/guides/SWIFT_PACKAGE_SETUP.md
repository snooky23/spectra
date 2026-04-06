# Swift Package Setup Guide

This guide explains how Spectra Logger is organized as a Swift Package and how to integrate the unified UI SDK.

## Package Structure

Spectra Logger is now distributed as a single Swift Package containing two binary products:

### 1. SpectraLogger (Core Logic)
The compiled Kotlin Multiplatform logging engine. Handles all storage, interception, and background processing.

### 2. SpectraLoggerUI (Unified UI SDK)
The compiled Compose Multiplatform UI SDK. This provides the exact same powerful log viewer on iOS as on Android.

---

## Integration Instructions

### 1. Add Package Dependency
Add the Spectra Logger repository to your Xcode project or `Package.swift`:

```swift
.package(url: "https://github.com/snooky23/spectra.git", from: "1.0.5")
```

### 2. Create the SwiftUI Bridge
Since the UI SDK is a Compose Multiplatform binary, you must wrap its exported `UIViewController` in a SwiftUI `UIViewControllerRepresentable`.

Create `SpectraLoggerView.swift` in your app:

```swift
import SwiftUI
import UIKit
import SpectraLogger
import SpectraLoggerUI

struct SpectraLoggerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // 1. Initialize the UI module bridge
        // This registers the view controller provider with the core module
        SpectraUI.shared.init()
        
        // 2. Return the shared view controller
        return SpectraLoggerViewControllerKt.SpectraLoggerViewController(onDismiss: {
            // Use the shared manager to handle dismissal state
            SpectraUIManager.shared.dismissScreen()
        })
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### 3. Present the Logger
You can now use `SpectraLoggerView` like any other SwiftUI view:

```swift
struct MainView: View {
    @State private var showLogs = false
    
    var body: some View {
        Button("Open Debug Logs") {
            showLogs = true
        }
        .sheet(isPresented: $showLogs) {
            SpectraLoggerView()
        }
    }
}
```

---

## Architecture Overview

The new unified architecture ensures that iOS and Android developers get the exact same feature set and user experience simultaneously.

```
┌─────────────────────┐
│   Your iOS App      │ (SwiftUI / Swift)
└────────┬────────────┘
         │ imports
┌────────▼─────────────────────────────────────┐
│   SpectraLoggerUI (Binary)                    │ (Compose Multiplatform)
│   • Unified Adaptive UI                       │
│   • List-Detail Pane Scaffold                 │
└────────┬─────────────────────────────────────┘
         │ depends on
┌────────▼─────────────────────────────────────┐
│   SpectraLogger (Binary)                      │ (Kotlin Multiplatform)
│   • Core Logging Logic                        │
│   • XCFramework wrapper                       │
└───────────────────────────────────────────────┘
```

---

## Local Development

If you are building from source, you must generate the XCFrameworks locally before they can be consumed by the example app or your project.

```bash
# Build both Core and UI frameworks
./scripts/build/build-xcframework.sh
```

The resulting frameworks will be placed in `build/xcframework/`.

---

## Troubleshooting

### "Missing module SpectraLoggerUI"
- Ensure you have selected both `SpectraLogger` and `SpectraLoggerUI` in the "Frameworks, Libraries, and Embedded Content" section of your target settings.
- Verify your minimum deployment target is **iOS 15.0** or higher.

### Checksum Errors
If you manually updated `Package.swift` and see a checksum error, run:
```bash
swift package compute-checksum build/xcframework/SpectraLoggerUI.xcframework.zip
```
And ensure the output matches the `checksum` field in `Package.swift`.
