# SpectraLogger - Core Framework

This is the Swift Package wrapper for the Kotlin Multiplatform core logging framework.

## Structure

This package provides the compiled KMP framework as a binary target, making it easy to use from Swift Package Manager.

```
SpectraLogger/
├── Package.swift                    # Swift Package manifest
├── XCFrameworks/
│   ├── release/
│   │   └── SpectraLogger.xcframework/
│   └── debug/
│       └── SpectraLogger.xcframework/
└── README.md
```

## Building the XCFramework

The XCFramework must be built from the Kotlin Multiplatform shared module before this package can be used.

### Release Build (Recommended)

```bash
cd ../shared
./gradlew shared:linkReleaseFrameworkIosArm64 \
          shared:linkReleaseFrameworkIosSimulatorArm64 \
          shared:createXCFramework

# The XCFramework will be created at:
# shared/build/XCFrameworks/release/SpectraLogger.xcframework
```

### Debug Build

```bash
cd ../shared
./gradlew shared:linkDebugFrameworkIosArm64 \
          shared:linkDebugFrameworkIosSimulatorArm64

# Then manually create XCFramework:
mkdir -p ../SpectraLogger/XCFrameworks/debug
xcodebuild -create-xcframework \
  -framework build/bin/iosArm64/debugFramework/SpectraLogger.framework \
  -framework build/bin/iosSimulatorArm64/debugFramework/SpectraLogger.framework \
  -output ../SpectraLogger/XCFrameworks/debug/SpectraLogger.xcframework
```

## Installation

### Add to Your Package

In your `Package.swift`:

```swift
dependencies: [
    .package(path: "../SpectraLogger")
]
```

### Add to Your Xcode Project

1. File → Add Packages
2. Enter the path: `/path/to/SpectraLogger`
3. Select "Up to Next Major Version" or specify version
4. Add to your target

## Usage

Once added, import and use:

```swift
import SpectraLogger

// Initialize the logger (call once at app startup)
SpectraLoggerKt.initialize(config: SpectraConfig(
    minLogLevel: .debug,
    maxInMemoryLogs: 10000,
    enableConsoleLogging: true
))

// Use the logger
SpectraLoggerKt.i(tag: "MyApp", message: "App started")
SpectraLoggerKt.e(tag: "Network", message: "Request failed", error: error)
```

## Dependency

This package has no external dependencies - it only provides the compiled KMP framework.

For the UI components, see [SpectraLoggerUI](../SpectraLoggerUI/README.md) which depends on this package.

## Architecture Support

- **iOS Device**: arm64
- **iOS Simulator (Apple Silicon)**: arm64-simulator
- **iOS Simulator (Intel)**: x86_64

The included XCFramework supports all three architectures in a single package.

## Notes

- The XCFramework must be in the correct location for Swift Package Manager to find it
- If you move this package, update the path in Package.swift accordingly
- Ensure the framework is built before trying to use this package in Xcode
