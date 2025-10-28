# Swift Package Setup Guide

This guide explains how Spectra Logger is organized as Swift Packages and how to use it.

## Package Structure

Spectra Logger is organized into two complementary Swift Packages:

### 1. SpectraLogger (Core Framework)

**Location**: `/SpectraLogger`

This package wraps the Kotlin Multiplatform compiled XCFramework as a binary target.

```
SpectraLogger/
├── Package.swift
├── README.md
└── XCFrameworks/
    ├── release/SpectraLogger.xcframework
    └── debug/SpectraLogger.xcframework
```

**Contents**: The compiled KMP logging framework
- Application event logging
- Network request/response logging
- Core storage and filtering

**Dependencies**: None (binary only)

**Use when**: You want just the core logging functionality

### 2. SpectraLoggerUI (UI Components)

**Location**: `/SpectraLoggerUI`

SwiftUI components for viewing and managing logs.

```
SpectraLoggerUI/
├── Package.swift
├── README.md
├── Sources/
│   └── SpectraLoggerUI/
│       ├── SpectraLoggerView.swift    (Public API entry point)
│       ├── Views/
│       │   ├── LogsView.swift
│       │   ├── NetworkLogsView.swift
│       │   └── SettingsView.swift
│       └── ViewModels/
│           ├── LogsViewModel.swift
│           ├── NetworkLogsViewModel.swift
│           └── SettingsViewModel.swift
└── Tests/
```

**Contents**: SwiftUI views and view models
- Logs tab with search and filtering
- Network logs tab with request/response details
- Settings tab with configuration options
- Complete, production-ready UI

**Dependencies**:
- SpectraLogger (core framework)

**Use when**: You want the complete logging UI

### 3. Example App (SpectraExample)

**Location**: `/examples/ios-native`

A complete example app showing how to integrate Spectra Logger.

```
examples/ios-native/
├── Package.swift
├── SpectraExample/
│   ├── SpectraExampleApp.swift
│   ├── MainAppView.swift
│   └── Assets.xcassets/
└── README.md
```

**Dependencies**:
- SpectraLoggerUI (which includes SpectraLogger)

---

## Setup Instructions

### Prerequisites

1. **Build the XCFramework** (one time only):

```bash
cd shared
./gradlew shared:linkReleaseFrameworkIosArm64 \
          shared:linkReleaseFrameworkIosSimulatorArm64 \
          shared:createXCFramework
```

This creates: `shared/build/XCFrameworks/release/SpectraLogger.xcframework`

2. **Copy to SpectraLogger package**:

```bash
# The framework needs to be in the SpectraLogger package
# For now, it's at: shared/build/XCFrameworks/release/SpectraLogger.xcframework
#
# Future: We can automate this with a build script or symlink
```

### Option A: Use Only Core Framework

Add to your `Package.swift`:

```swift
dependencies: [
    .package(path: "../SpectraLogger")
]
```

In your target:

```swift
.target(
    name: "MyApp",
    dependencies: [
        .product(name: "SpectraLogger", package: "SpectraLogger")
    ]
)
```

### Option B: Use Core + UI (Recommended)

Add to your `Package.swift`:

```swift
dependencies: [
    .package(path: "../SpectraLoggerUI")
]
```

In your target:

```swift
.target(
    name: "MyApp",
    dependencies: [
        .product(name: "SpectraLoggerUI", package: "SpectraLoggerUI")
    ]
)
```

### Option C: In an Xcode Project (Not Swift Package)

1. File → Add Packages
2. Click "Add Local"
3. Select `/SpectraLoggerUI` folder
4. Select your target and add

---

## Architecture

```
┌─────────────────────┐
│   Your iOS App      │
└────────┬────────────┘
         │ imports
┌────────▼─────────────────────────────────────┐
│   SpectraLoggerUI                             │
│   (SwiftUI views + ViewModels)                │
│   • LogsView                                  │
│   • NetworkLogsView                           │
│   • SettingsView                              │
└────────┬─────────────────────────────────────┘
         │ depends on
┌────────▼─────────────────────────────────────┐
│   SpectraLogger (Core)                        │
│   (Swift Package wrapping XCFramework)        │
│   • Binary target: SpectraLogger.xcframework  │
│   • Kotlin Multiplatform compiled for iOS    │
└───────────────────────────────────────────────┘
```

---

## Dependency Resolution

Swift Package Manager automatically resolves the dependency chain:

```
SpectraExample
  → depends on SpectraLoggerUI
    → depends on SpectraLogger
      → XCFramework (binary)
```

When you add SpectraLoggerUI to your app, you automatically get access to SpectraLogger.

---

## Building & Testing

### Test the Core Framework

```bash
cd SpectraLogger
swift build
swift test
```

### Test the UI Package

```bash
cd SpectraLoggerUI
swift build
swift test
```

### Test the Example App

```bash
cd examples/ios-native
open Package.swift
# Cmd+R in Xcode
```

### Run Kotlin Tests (Validates iOS functionality)

```bash
./gradlew test
./gradlew iosTest
```

---

## Troubleshooting

### "Cannot find module 'SpectraLogger'"

The XCFramework must be built and in the correct location.

```bash
cd shared
./gradlew shared:linkReleaseFrameworkIosArm64 \
          shared:linkReleaseFrameworkIosSimulatorArm64 \
          shared:createXCFramework
```

### "Package path not found"

Make sure the relative paths in Package.swift are correct:

```swift
// Correct (from SpectraLoggerUI):
.package(path: "../SpectraLogger")

// Correct (from SpectraExample):
.package(path: "../../SpectraLoggerUI")
```

### XCFramework build fails

Ensure you're building both architectures:

```bash
./gradlew shared:linkReleaseFrameworkIosArm64 \
          shared:linkReleaseFrameworkIosSimulatorArm64 \
          shared:createXCFramework
```

The XCFramework must include:
- `ios-arm64` (physical devices)
- `ios-arm64-simulator` (Apple Silicon simulators)

---

## Publishing

When ready to publish:

1. **To CocoaPods** (if desired):
   - Use the existing `.podspec` files
   - Or create new ones for Swift Packages

2. **To Swift Package Registry** (Apple's official registry):
   - Create public GitHub repository
   - Tag releases with semantic versioning
   - Register with Swift Package Index

3. **To Maven Central** (for Kotlin/Java):
   - Core framework only (already configured)
   - See PUBLISHING.md

---

## Key Files

| File | Purpose |
|------|---------|
| `SpectraLogger/Package.swift` | Core framework package manifest |
| `SpectraLoggerUI/Package.swift` | UI package manifest |
| `examples/ios-native/Package.swift` | Example app manifest |
| `shared/build.gradle.kts` | Gradle config for XCFramework generation |

---

## Next Steps

1. ✅ Build XCFramework: `./gradlew shared:createXCFramework`
2. ⏳ Copy framework to SpectraLogger package XCFrameworks directory
3. ⏳ Test with example app: `open examples/ios-native/Package.swift`
4. ⏳ Verify all packages resolve correctly
5. ⏳ Run end-to-end tests

---

**Last Updated**: October 27, 2025
