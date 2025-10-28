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

#### Local Development (During Development)

Add to your `Package.swift`:

```swift
dependencies: [
    .package(path: "../SpectraLoggerUI")  // Local development
]
```

#### Release Mode (Using Published Package)

```swift
dependencies: [
    .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0")
]
```

In your target (same for both local and release):

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

## Local Development vs Release Mode

Spectra Logger packages support both local development and release modes.

### Local Development Mode (Default)

**When**: Developing both packages together in the same workspace

**Configuration**:

In `SpectraLoggerUI/Package.swift`:
```swift
dependencies: [
    .package(path: "../SpectraLogger"),  // ← Local development
]
```

In `examples/ios-native/Package.swift`:
```swift
dependencies: [
    .package(path: "../../SpectraLoggerUI"),  // ← Local development
]
```

**Advantages**:
- Live changes to core or UI are immediately reflected
- No need to rebuild and republish
- Perfect for debugging both packages together
- Can test integration in real-time

**How to switch TO local mode**:
1. Ensure both packages are in the same workspace
2. Edit `Package.swift`
3. Use `.package(path: "...")` with relative paths
4. Run `swift build` or open in Xcode

### Release Mode

**When**: Using published, released versions of packages

**Configuration**:

In `SpectraLoggerUI/Package.swift`:
```swift
dependencies: [
    .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0"),
]
```

**Advantages**:
- Uses stable, vetted releases
- No workspace structure required
- Can be used in any project
- Version pinning for reproducibility
- Automatic updates based on version requirements

**How to switch TO release mode**:

1. **Replace path dependency with URL**:
   ```swift
   // From:
   .package(path: "../SpectraLogger")

   // To:
   .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0")
   ```

2. **Pin to specific version**:
   ```swift
   // Exact version:
   .package(url: "https://github.com/snooky23/spectra.git", .exact("1.0.0"))

   // Range:
   .package(url: "https://github.com/snooky23/spectra.git", "1.0.0"..<"2.0.0")

   // Up to next major:
   .package(url: "https://github.com/snooky23/spectra.git", .upToNextMajor(from: "1.0.0"))
   ```

3. **Run package resolution**:
   ```bash
   swift package resolve
   ```

### Quick Reference: Mode Switching

**Local → Release**:
```diff
- .package(path: "../SpectraLoggerUI")
+ .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0")
```

**Release → Local**:
```diff
- .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0")
+ .package(path: "../SpectraLoggerUI")
```

### Repository Structure for Development

To use local development mode, organize your workspace like this:

```
workspace/
├── spectra/                          # Main repo (this repository)
│   ├── SpectraLogger/               # Core package
│   ├── SpectraLoggerUI/             # UI package
│   └── examples/
│       └── ios-native/              # Example app
└── my-app/                          # Your app using local development
    ├── Package.swift                # Points to ../spectra/SpectraLoggerUI
    └── Sources/
```

Or if everything is in one repo:

```
spectra/
├── SpectraLogger/
│   └── Package.swift                # Points to ../SpectraLoggerUI (release) OR local
├── SpectraLoggerUI/
│   └── Package.swift                # Points to ../SpectraLogger
├── examples/
│   └── ios-native/
│       └── Package.swift            # Points to ../../SpectraLoggerUI (local)
└── shared/                          # Kotlin Multiplatform module
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
