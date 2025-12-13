# SpectraLoggerUI - Testing Guide

This guide covers all testing approaches for the SpectraLoggerUI framework and its integration with the iOS example app.

## Quick Start

### 1️⃣ Test the Swift Package Only

If you only want to test the UI components themselves:

```bash
cd SpectraLoggerUI
swift test
```

### 2️⃣ Test the Full iOS Example App

If you want to test the complete integration with the KMP core framework:

```bash
cd examples/ios-native
./setup-pods.sh
open SpectraExample.xcworkspace
# Then build and run in Xcode (Cmd+R)
```

---

## Part 1: Testing the Swift Package

The SpectraLoggerUI package can be tested independently using Swift's testing framework.

### Prerequisites

- Xcode 14.0+
- Swift 5.9+
- macOS 12+

### Running Tests

#### Run All Tests

```bash
cd SpectraLoggerUI
swift test
```

Expected output:
```
Building for debugging...
Build complete! (X.XXs)
Test Suite 'SpectraLoggerUITests' started at YYYY-MM-DD HH:MM:SS.XXX
Test Suite 'SpectraLoggerUITests' passed at YYYY-MM-DD HH:MM:SS.XXX
Executed 1 test, with 0 failures (0 unexpected) in 0.000 (0.XXX) wall clock time
```

#### Run Specific Test File

```bash
swift test SpectraLoggerUITests
```

#### Run Tests with Verbose Output

```bash
swift test --verbose
```

This shows detailed output including which tests are running:
```
Test Suite 'SpectraLoggerUITests' started at ...
  Test Case 'SpectraLoggerUITests.example' started at ...
  Test Case 'SpectraLoggerUITests.example' passed (0.001 seconds)
Test Suite 'SpectraLoggerUITests' passed (0.002 seconds)
```

#### Run Tests with Code Coverage

```bash
swift test --code-coverage
```

The coverage report is generated in:
```
.build/debug/codecov/SpectraLoggerUI.json
```

View the HTML report:
```bash
# Coverage data is in .build directory
open .build/debug/codecov/
```

#### Run Tests in Release Mode

```bash
swift test -c release
```

### Writing New Tests

Tests are located in `Tests/SpectraLoggerUITests/SpectraLoggerUITests.swift`

Example test structure:

```swift
import Testing
@testable import SpectraLoggerUI

@Test func testLogsViewRendering() async throws {
    let view = LogsView()

    // Your test assertions here
    #expect(view != nil)
}
```

### Common Test Scenarios

#### Test View Models

```swift
@Test func testLogsViewModelInitialization() async throws {
    let viewModel = LogsViewModel()

    #expect(viewModel.logs.isEmpty)
    #expect(viewModel.isLoading == false)
}
```

#### Test View Rendering

```swift
@Test func testNetworkLogsViewRenders() async throws {
    let view = NetworkLogsView()

    // Views can be snapshot tested or rendered in Canvas
    #expect(view != nil)
}
```

---

## Part 2: Testing the iOS Example App

The iOS example app demonstrates how to integrate SpectraLoggerUI with the KMP core framework.

### Prerequisites

- Xcode 15.0+
- iOS 15.0+ simulator (or physical device)
- Ruby 2.7+ (for CocoaPods)
- Gradle 8.5+ (for Kotlin/Native framework building)

### Step 1: Setup CocoaPods

From the `examples/ios-native` directory:

```bash
# This script automatically builds the KMP framework
./setup-pods.sh
```

What it does:
1. ✅ Checks if CocoaPods is installed
2. ✅ Runs `pod install` to resolve dependencies
3. ✅ Automatically builds the KMP framework via the Podfile's `prepare_command`
4. ✅ Creates `SpectraExample.xcworkspace`

### Step 2: Open the Workspace in Xcode

**⚠️ IMPORTANT**: Always use `.xcworkspace`, NOT `.xcodeproj`

```bash
open SpectraExample.xcworkspace
```

Why? CocoaPods requires the workspace to properly link dependencies.

### Step 3: Build and Run in Xcode

1. **Select a simulator**:
   - Click on the simulator dropdown at the top
   - Choose "iPhone 15 Pro" (recommended)

2. **Build the app**:
   - Press **Cmd+B** to build

3. **Run the app**:
   - Press **Cmd+R** to build and run
   - Or click the Play button ▶️

### Step 4: Manual Testing

Once the app launches in the simulator:

#### Test App Logs

1. **Generate logs**:
   - Tap the "Tap Me (Generates Log)" button
   - Tap "Generate Warning" button
   - Tap "Generate Error" button

2. **Open Logger**:
   - Tap the purple "Open Spectra Logger" button

3. **View Logs Tab**:
   - All generated logs should appear in the list
   - Tap a log to see full details
   - Use search to filter logs
   - Use filter chips to filter by level

#### Test Network Logs Tab

**Note**: Network logging requires additional URLSession setup. See [Network Logging Setup](#network-logging-setup) below.

#### Test Settings Tab

1. **Change appearance**:
   - Toggle between Light/Dark/System modes
   - UI should update immediately

2. **View storage stats**:
   - See how many app and network logs are stored
   - Stats update in real-time

3. **Clear logs**:
   - Tap "Clear Logs"
   - Confirm the action
   - Log list should be empty

4. **Export logs**:
   - Tap "Export Logs" (if implemented)
   - Choose export format

### Network Logging Setup

To test network logging, you need to set up URLSession interception:

```swift
// In your app initialization
import SpectraLogger

// Enable network logging
SpectraLoggerKt.registerNetworkInterceptor()

// Then use URLSession normally
let session = URLSession(configuration: .default)
let (data, response) = try await session.data(from: URL(string: "https://api.example.com")!)
```

Network requests will now appear in the "Network" tab of Spectra Logger.

### URL Scheme Testing

The app supports deep linking via URL schemes:

```bash
# Open logs screen
xcrun simctl openurl booted spectralogger://logs

# Open network logs
xcrun simctl openurl booted spectralogger://network

# Clear logs
xcrun simctl openurl booted spectralogger://clear
```

Or from Safari in the simulator:
- Type `spectralogger://logs` in the address bar

### Debugging Tips

#### View Build Output

```bash
# See detailed build logs
xcodebuild -workspace SpectraExample.xcworkspace \
           -scheme SpectraExample \
           -configuration Debug \
           -sdk iphonesimulator \
           -verbose
```

#### Check Pod Installation

```bash
# Verify pods are installed correctly
ls Pods/SpectraLogger/
ls Pods/SpectraLoggerUI/

# Or check the Podfile.lock
cat Podfile.lock | grep -A 5 SpectraLogger
```

#### Reset Build Cache

If you encounter build issues:

```bash
# Clean Xcode cache
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# Clean Pods cache
rm -rf Pods
rm Podfile.lock

# Rebuild everything
./setup-pods.sh
open SpectraExample.xcworkspace
```

---

## Part 3: Running Core Kotlin Tests

The shared KMP module includes comprehensive tests that also validate iOS functionality.

### Prerequisites

- Java 17+
- Gradle 8.5+
- Kotlin 1.9+

### Run All Tests

```bash
cd /path/to/Spectra/root
./gradlew test
```

Expected output:
```
> Task :shared:commonTest
> Task :shared:androidUnitTest
> Task :shared:iosTest
BUILD SUCCESSFUL
```

### Run iOS-Specific Tests

```bash
./gradlew iosTest
```

### Run Tests with Coverage

```bash
./gradlew koverHtmlReport
```

Report location: `shared/build/reports/kover/html/index.html`

### Run Specific Test Class

```bash
./gradlew test --tests "*LogStorageTest*"
```

---

## Part 4: Continuous Integration Testing

### GitHub Actions

The repository includes CI workflows that automatically test on every commit:

```bash
# Locally simulate the CI pipeline
act -j build  # Requires 'act' tool
```

### Local Lint and Format Check

```bash
# Run code quality checks
./scripts/code-quality.sh

# Format code
./gradlew ktlintFormat
```

---

## Troubleshooting Test Failures

### Swift Package Tests

#### Error: "No such module 'SpectraLoggerUI'"

```bash
# Rebuild the package
swift build -c debug

# Then run tests
swift test
```

#### Error: "linker command failed"

This usually means the KMP framework is missing. Ensure it's built:

```bash
cd ../..
./gradlew shared:linkReleaseFrameworkIosArm64 shared:linkReleaseFrameworkIosSimulatorArm64
```

### iOS Example App

#### Error: "Unable to find module dependency: 'SpectraLoggerUI'"

1. Close Xcode
2. Clean everything:
   ```bash
   rm -rf Pods Podfile.lock
   rm -rf ~/Library/Developer/Xcode/DerivedData/*
   ```
3. Rebuild:
   ```bash
   ./setup-pods.sh
   open SpectraExample.xcworkspace
   ```

#### Error: "ld: framework not found SpectraLogger"

The KMP framework wasn't built. From the root directory:

```bash
./gradlew shared:linkReleaseFrameworkIosArm64 shared:linkReleaseFrameworkIosSimulatorArm64 shared:createXCFramework
```

#### Simulator Crashes on Launch

1. Kill the simulator process:
   ```bash
   xcrun simctl shutdown all
   xcrun simctl erase all
   ```

2. Reopen and rebuild:
   ```bash
   open SpectraExample.xcworkspace
   # Cmd+B then Cmd+R
   ```

### Kotlin Tests

#### Test Timeout

Increase timeout in `build.gradle.kts`:

```kotlin
tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
}
```

#### Memory Issues

```bash
# Run with more heap space
./gradlew test -Xmx4g
```

---

## Performance Benchmarking

### Measure Log Capture Time

The shared module includes performance tests:

```bash
./gradlew test --tests "*PerformanceTest*"
```

Expected results:
- Log capture: **< 1ms**
- Network interception: **< 5ms**
- UI rendering: **60 FPS**

### Profile in Xcode

1. Run the app: Cmd+R
2. Open Xcode's Profiler: Cmd+I
3. Select **System Trace** or **Time Profiler**
4. Interact with the app
5. Stop recording to analyze

---

## Testing Checklist

- [ ] Swift package tests pass: `swift test`
- [ ] Example app builds: `Cmd+B`
- [ ] Example app runs: `Cmd+R`
- [ ] App logs display in UI
- [ ] Search/filter works
- [ ] Settings tab functions
- [ ] Clear logs works
- [ ] Export logs works
- [ ] Dark mode toggle works
- [ ] Kotlin tests pass: `./gradlew test`
- [ ] Code quality checks pass: `./scripts/code-quality.sh`
- [ ] No build warnings
- [ ] No UI glitches or crashes

---

## Next Steps

1. **Add Unit Tests**: Expand `SpectraLoggerUITests` with comprehensive view model and view tests
2. **Add UI Tests**: Use `XCUITest` framework for automated UI testing
3. **Add Snapshot Tests**: Use `SnapshotTesting` library for view rendering validation
4. **Performance Profiling**: Measure frame rates during log scrolling
5. **Device Testing**: Test on physical iPhone/iPad devices

---

## Resources

- [Apple Testing Documentation](https://developer.apple.com/documentation/xcode/testing)
- [Swift Testing Framework](https://github.com/apple/swift-testing)
- [CocoaPods Guide](https://guides.cocoapods.org/)
- [Kotlin Multiplatform Testing](https://kotlinlang.org/docs/multiplatform-run-tests.html)

---

**Last Updated**: October 27, 2025
