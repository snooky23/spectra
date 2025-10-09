# SpectraLoggerUI

Native iOS UI for Spectra Logger - A Swift Package providing SwiftUI screens for viewing application and network logs.

## Overview

SpectraLoggerUI is the iOS-native UI layer for Spectra Logger. It wraps the Kotlin Multiplatform core framework and provides beautiful, native SwiftUI screens for:

- **Application Logs**: View, filter, and search app logs with native iOS design
- **Network Logs**: Monitor HTTP requests/responses with detailed headers and bodies
- **Settings**: Configure appearance (Light/Dark/System), manage storage, export logs

## Architecture

```
┌─────────────────────────────┐
│   Your iOS App              │
│   (SwiftUI/UIKit)          │
└──────────┬──────────────────┘
           │ imports
┌──────────▼──────────────────┐
│   SpectraLoggerUI           │  ← This Package (Swift)
│   (Swift Package)           │
│   - SwiftUI Views           │
│   - ViewModels              │
└──────────┬──────────────────┘
           │ depends on
┌──────────▼──────────────────┐
│   SpectraLogger.xcframework │  ← KMP Core (Kotlin → iOS)
│   (Binary Target)           │
│   - Business Logic          │
│   - Storage                 │
│   - Models                  │
└─────────────────────────────┘
```

## Installation

### Swift Package Manager (Xcode)

1. In Xcode, go to **File → Add Package Dependencies**
2. Enter the repository URL: `https://github.com/yourname/Spectra`
3. Select `SpectraLoggerUI` from the list of products
4. Click **Add Package**

### Local Development

```swift
// In your Package.swift
dependencies: [
    .package(path: "../SpectraLoggerUI")
]
```

## Usage

### Basic Usage

```swift
import SwiftUI
import SpectraLoggerUI

struct ContentView: View {
    @State private var showLogger = false

    var body: some View {
        Button("Open Logger") {
            showLogger = true
        }
        .sheet(isPresented: $showLogger) {
            SpectraLoggerView()
        }
    }
}
```

### Full Screen

```swift
.fullScreenCover(isPresented: $showLogger) {
    SpectraLoggerView()
}
```

### In Navigation

```swift
NavigationLink("View Logs") {
    SpectraLoggerView()
}
```

## Features

### Logs Tab
- ✅ Real-time log updates
- ✅ Filter by level (Verbose, Debug, Info, Warning, Error, Fatal)
- ✅ Search by message, tag, or metadata
- ✅ Tap to see full details including stack traces
- ✅ Native iOS design with SF Symbols

### Network Tab
- ✅ All HTTP requests/responses
- ✅ Filter by method (GET, POST, PUT, DELETE, PATCH)
- ✅ Filter by status code (2xx, 3xx, 4xx, 5xx)
- ✅ View headers and bodies
- ✅ Request duration tracking

### Settings Tab
- ✅ Appearance: Light, Dark, or System mode
- ✅ Storage statistics
- ✅ Clear logs
- ✅ Export functionality (coming soon)

## Requirements

- iOS 15.0+
- Xcode 14.0+
- Swift 5.9+

## Building from Source

### Prerequisites

1. Build the KMP XCFramework first:

```bash
cd ../shared
./gradlew :shared:linkReleaseFrameworkIosArm64 \
          :shared:linkReleaseFrameworkIosSimulatorArm64 \
          :shared:linkReleaseFrameworkIosX64
```

2. Create the XCFramework:

```bash
# Combine simulator architectures
lipo -create \
  build/bin/iosSimulatorArm64/releaseFramework/SpectraLogger.framework/SpectraLogger \
  build/bin/iosX64/releaseFramework/SpectraLogger.framework/SpectraLogger \
  -output build/bin/iosX64/releaseFramework/SpectraLogger.framework/SpectraLogger

# Create XCFramework
xcodebuild -create-xcframework \
  -framework build/bin/iosArm64/releaseFramework/SpectraLogger.framework \
  -framework build/bin/iosX64/releaseFramework/SpectraLogger.framework \
  -output build/XCFrameworks/release/SpectraLogger.xcframework
```

3. Open in Xcode:

```bash
cd ../SpectraLoggerUI
open Package.swift
```

## Development

### Live Previews

All SwiftUI views support Xcode Canvas previews:

```swift
#Preview {
    LogsView()
}
```

### Project Structure

```
SpectraLoggerUI/
├── Package.swift                      # Package manifest
├── Sources/
│   └── SpectraLoggerUI/
│       ├── SpectraLoggerView.swift   # Public API entry point
│       ├── Views/
│       │   ├── LogsView.swift
│       │   ├── NetworkLogsView.swift
│       │   └── SettingsView.swift
│       └── ViewModels/
│           ├── LogsViewModel.swift
│           ├── NetworkLogsViewModel.swift
│           └── SettingsViewModel.swift
└── Tests/
    └── SpectraLoggerUITests/
```

## License

[Your License]

## Contributing

Contributions welcome! Please read CONTRIBUTING.md first.
