# Spectra Logger - iOS Native Example

This example demonstrates how to use Spectra Logger in a native iOS application using Swift and SwiftUI. It uses the unified **Compose Multiplatform** UI SDK for a consistent debugging experience.

## Prerequisites

- Xcode 15.0 or later
- iOS 15.0 or later
- macOS with Apple Silicon or Intel processor

## Setup

### Quick Start (Swift Package Manager)

1. **Ensure the XCFrameworks are built**:
   From the project root, run:
   ```bash
   ./scripts/build/build-xcframework.sh
   ```
   This will use the official KMP Gradle tasks to build `SpectraLogger` and `SpectraLoggerUI` frameworks and place them in `build/xcframework/`.

2. **Open the Xcode project**:
   ```bash
   cd examples/ios-native
   open SpectraExample.xcodeproj
   ```

3. **Build and run**:
   - Select an iPhone simulator.
   - Press **Cmd+R** to run.

### How Dependencies Are Managed

This example app uses **Swift Package Manager** to depend on:
- `SpectraLogger` (Core Logic) - Binary XCFramework
- `SpectraLoggerUI` (Unified UI) - Binary XCFramework

The root `Package.swift` is configured to look for these frameworks in `build/xcframework/` during local development.

## Features Demonstrated

### Core Logging Features
- ✅ **All Log Levels**: Verbose, Debug, Info, Warning, Error, Fatal
- ✅ **Structured Logging**: Key-value metadata support
- ✅ **Error Tracking**: Full stack trace capture with line numbers
- ✅ **Adaptive UI**: Unified Compose Multiplatform UI that works identically on iOS and Android.

### UI Features
- ✅ **Logs Tab**: Real-time log display with multi-level filtering and search.
- ✅ **Network Tab**: HTTP request/response logging with header and body inspection.
- ✅ **Settings Tab**: Theme control, storage statistics, and log clearing.
- ✅ **Native Share Sheet**: Export logs via iOS native sharing.

## Project Structure

```
examples/ios-native/
├── SpectraExample.xcodeproj/          # Xcode project
├── SpectraExample/                    # Application source files
│   ├── SpectraExampleApp.swift        # App entry point
│   ├── MainAppView.swift              # Main screen
│   ├── SpectraLoggerView.swift        # SwiftUI wrapper for SpectraLoggerUI
│   └── ViewModels/                    # Demo logic
└── README.md                          # This file
```

## How It Works

### 1. Logger Initialization

In `SpectraExampleApp.swift`:
```swift
import SpectraLogger

@main
struct SpectraExampleApp: App {
    init() {
        SpectraLogger.shared.configure { config in
            config.appContext = AppContext(
                sessionId: UUID().uuidString,
                appVersion: "1.0.4",
                osName: "iOS",
                environment: "development"
            )
        }
    }
}
```

### 2. SwiftUI Integration

The `SpectraLoggerView.swift` file wraps the Kotlin-exported `UIViewController` using `UIViewControllerRepresentable`:

```swift
import SwiftUI
import SpectraLoggerUI

struct SpectraLoggerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return SpectraLoggerViewControllerKt.SpectraLoggerViewController(onDismiss: {
            // Handle dismissal
        })
    }
}
```

## Troubleshooting

### "Module Not Found" or Linker Errors
1. Ensure you have run `./scripts/build/build-xcframework.sh` from the project root.
2. Verify that `build/xcframework/` contains both `.xcframework` folders.
3. Clean the build folder in Xcode (**Cmd+Shift+K**).

### Signature Issues
Simulator builds do not require code signing. For physical devices, ensure your team is selected in the "Signing & Capabilities" tab.

### Runtime Crash (Compose Multiplatform)
If you encounter a crash with the message `CADisableMinimumFrameDurationOnPhone` is missing, ensure your `Info.plist` (or Target Build Settings) has this key:
- **Key**: `CADisableMinimumFrameDurationOnPhone`
- **Value**: `YES` (Boolean)

**Why is this required?**
Compose Multiplatform (via Skia) requires this to support **ProMotion (120Hz)** displays on modern iPhones. Without this key, iOS may throttle the third-party rendering loop to 60Hz, leading to synchronization issues and visible stuttering. The SDK enforces this check to ensure a premium, smooth user experience across all devices.

---

**Last Updated**: 2026-04-07
