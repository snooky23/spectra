# Spectra Logger - iOS Native Example

This example demonstrates how to use Spectra Logger in a native iOS application using Swift and SwiftUI.

## Prerequisites

- Xcode 15.0 or later
- iOS 15.0 or later
- macOS with Apple Silicon or Intel processor

## Setup

### Quick Start (Swift Package Manager)

1. **Ensure the XCFramework is built**:
   ```bash
   cd ../..
   ./gradlew shared:linkReleaseFrameworkIosArm64 shared:linkReleaseFrameworkIosSimulatorArm64 shared:createXCFramework
   ```

2. **Open the Xcode project**:
   ```bash
   cd examples/ios-native
   open SpectraExample.xcodeproj
   ```

3. **Build and run**:
   - Select iPhone simulator (iPhone 15 Pro recommended)
   - Press **Cmd+B** to build
   - Press **Cmd+R** to run

That's it! No CocoaPods setup needed.

### How Dependencies Are Managed

This example app uses **Swift Package Manager** to depend on:
- `SpectraLoggerUI` (local development) - located at `../../SpectraLoggerUI`
- `SpectraLogger` (core framework) - binary XCFramework

To switch to released versions, modify the package dependencies in Xcode project settings.

## Features Demonstrated

- **Main App Screen**: Example app with button to open Spectra Logger
- **Deep Linking**: URL scheme support to open logger from other apps
- **Logging**: All log levels (Verbose, Debug, Info, Warning, Error, Fatal)
- **Metadata**: Structured logging with key-value pairs
- **UI Components** (using Compose Multiplatform UI):
  - Log list screen with search and filtering
  - Network logs screen
  - Modern Material 3 UI components
- **Native SwiftUI Settings**:
  - Storage management
  - Log clearing
  - Export functionality
- **Framework Integration**: Shows how to use Kotlin Multiplatform framework from Swift

## Project Structure

```
examples/ios-native/
├── SpectraExample.xcodeproj/          # Xcode project (uses Swift Package Manager)
├── SpectraExample/                    # Application source files
│   ├── SpectraExampleApp.swift        # App entry point (@main)
│   ├── MainAppView.swift              # Main screen with Spectra Logger button
│   ├── SpectraLoggerView.swift        # Wrapper for SpectraLoggerUI
│   ├── Assets.xcassets/               # App icons and images
│   └── ViewModels/                    # View model files
├── README.md                          # This file
└── (no Podfile or .xcworkspace - uses native SPM)
```

**Dependencies** (managed via Xcode project settings):
- `SpectraLoggerUI` → depends on `SpectraLogger` → XCFramework binary

## How It Works

### 1. Framework Linking

**With CocoaPods**:
- The framework is managed as a local pod via `Podfile`
- CocoaPods automatically builds the framework using the `prepare_command` in `SpectraLogger.podspec`
- Framework is located at `../../shared/build/cocoapods/framework/SpectraLogger.framework`

**With Direct Linking**:
- **FRAMEWORK_SEARCH_PATHS**: Points to `../../shared/build/bin/iosSimulatorArm64/debugFramework`
- **Embedded Framework**: `SpectraLogger.framework` is embedded in the app bundle

### 2. Logger Initialization

In `SpectraExampleApp.swift`:
```swift
SpectraLoggerKt.doInitialize(
    config: SpectraConfig(
        minLogLevel: LogLevel.verbose,
        maxInMemoryLogs: 10000,
        maxNetworkLogs: 1000,
        enableConsoleLogging: true
    )
)
```

### 3. Logging from Swift

```swift
SpectraLoggerKt.i(tag: "User", message: "User opened the app")
SpectraLoggerKt.w(tag: "Performance", message: "Large dataset detected")
SpectraLoggerKt.e(tag: "Network", message: "Failed to fetch data")

// With metadata
let metadata = ["user_id": "12345", "platform": "iOS"]
SpectraLoggerKt.i(tag: "User", message: "Login successful", metadata: metadata)
```

### 4. Compose UI in SwiftUI

The log viewer uses Compose Multiplatform UI wrapped in SwiftUI:
```swift
struct ComposeViewController: UIViewControllerRepresentable {
    let viewController: UIViewController

    func makeUIViewController(context: Context) -> UIViewController {
        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed
    }
}
```

## Building for Different Targets

### iOS Simulator (ARM64 - Apple Silicon Macs)
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### iOS Simulator (X64 - Intel Macs)
```bash
./gradlew :shared:linkDebugFrameworkIosX64
```

### Physical Device (ARM64)
```bash
./gradlew :shared:linkDebugFrameworkIosArm64
```

After building for a different target, update the `FRAMEWORK_SEARCH_PATHS` in Xcode:
1. Select the project in Xcode
2. Go to Build Settings
3. Search for "Framework Search Paths"
4. Update the path to match your build target

## Troubleshooting

### Framework Not Found

**Error**: `ld: framework not found SpectraLogger`

**Solution**:
1. Build the framework for your target: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
2. Verify the framework exists: `ls ../../shared/build/bin/iosSimulatorArm64/debugFramework/`

### Module Not Found in Swift

**Error**: `No such module 'SpectraLogger'`

**Solution**:
1. Clean build folder: Product → Clean Build Folder (Cmd+Shift+K)
2. Verify Framework Search Paths in Build Settings
3. Rebuild the project

### Signature Issues

**Error**: Code signing errors

**Solution**:
1. Open project settings
2. Select your team in "Signing & Capabilities"
3. Or disable code signing for simulator builds

## URL Scheme Support

The app supports deep linking to open Spectra Logger from other apps or command line.

### Supported URLs

- `spectralogger://logs` - Opens the logs screen
- `spectralogger://network` - Opens the network logs screen
- `spectralogger://clear` - Clears all logs

### Setup URL Scheme

1. **Configure in Xcode**:
   - Open the project in Xcode
   - Select the SpectraExample target
   - Go to the "Info" tab
   - Add URL Type:
     - Identifier: `com.spectra.logger`
     - URL Schemes: `spectralogger`
     - Role: Editor

2. **Or run the helper script**:
   ```bash
   ./add-url-scheme.sh
   ```

### Testing URL Scheme

From Terminal (with simulator running):
```bash
# Open logs screen
xcrun simctl openurl booted spectralogger://logs

# Open network logs
xcrun simctl openurl booted spectralogger://network

# Clear logs
xcrun simctl openurl booted spectralogger://clear
```

From Safari on simulator:
- Just type the URL in the address bar: `spectralogger://logs`

From another iOS app:
```swift
if let url = URL(string: "spectralogger://logs") {
    UIApplication.shared.open(url)
}
```

## Network Logging

To enable network request logging, use the Spectra network interceptor:

```swift
// This feature requires platform-specific setup
// See shared module documentation for URLSession integration
```

## Export Logs

The export functionality demonstrates how to:
1. Query all logs from storage
2. Format logs as structured text
3. Present iOS share sheet
4. Save logs to Files app

## Performance

The example generates 30+ sample logs on startup to demonstrate:
- Fast log capture (< 1ms per log)
- Efficient in-memory storage
- Smooth UI with large log sets
- Search and filtering performance

## Testing

### Quick Test Commands

```bash
# From examples/ios-native directory

# Option 1: Fresh setup from scratch
./setup-pods.sh
open SpectraExample.xcworkspace

# Option 2: Update if already set up
pod install
open SpectraExample.xcworkspace

# Then in Xcode: Cmd+R to build and run
```

### Manual Testing Checklist

Once the app is running in the simulator, test these features:

#### App Logs Tab ✅
- [ ] Launch the app
- [ ] Tap "Tap Me" button - should generate logs
- [ ] Tap "Generate Warning" - should add warning log
- [ ] Tap "Generate Error" - should add error log
- [ ] Logs appear in real-time
- [ ] Tap a log to view full details
- [ ] Search logs by message
- [ ] Filter logs by level (Verbose, Debug, Info, Warning, Error, Fatal)

#### Network Logs Tab ✅
- [ ] Network requests appear (if URLSession is integrated)
- [ ] Filter by HTTP method (GET, POST, PUT, DELETE, PATCH)
- [ ] Filter by status code ranges (2xx, 3xx, 4xx, 5xx)
- [ ] Tap network request to view details
- [ ] View request headers and body
- [ ] View response headers and body
- [ ] Copy cURL command for request

#### Settings Tab ✅
- [ ] Toggle appearance (Light/Dark/System)
- [ ] View app log count
- [ ] View network log count
- [ ] Tap "Clear Logs" button
- [ ] Confirm clear action
- [ ] Logs are cleared successfully
- [ ] Export logs (if implemented)

#### URL Scheme Deep Linking ✅
```bash
# From terminal while simulator is running

# Open logs screen
xcrun simctl openurl booted spectralogger://logs

# Open network logs
xcrun simctl openurl booted spectralogger://network

# Clear all logs
xcrun simctl openurl booted spectralogger://clear
```

### Automated Testing

#### Build from Command Line

```bash
# Build the app
xcodebuild -workspace SpectraExample.xcworkspace \
           -scheme SpectraExample \
           -configuration Debug \
           -sdk iphonesimulator \
           -derivedDataPath build

# Or simply
xcodebuild build-for-testing \
           -workspace SpectraExample.xcworkspace \
           -scheme SpectraExample \
           -configuration Debug
```

#### Run on Physical Device

```bash
# Build for physical device (arm64)
xcodebuild -workspace SpectraExample.xcworkspace \
           -scheme SpectraExample \
           -configuration Release \
           -sdk iphoneos \
           -derivedDataPath build
```

### Rebuilding After Changes

If you modify the shared KMP module:

```bash
# Update the framework
pod update SpectraLogger

# If that doesn't work, clean everything
rm -rf Pods
rm Podfile.lock
./setup-pods.sh

# Then in Xcode: Clean (Cmd+Shift+K) and rebuild (Cmd+B)
```

### Debugging

#### View Console Logs

1. In Xcode: View → Debug Area → Show Console (Cmd+Shift+Y)
2. Run the app
3. All print statements and framework logs appear here

#### Enable Detailed Logging

In `SpectraExampleApp.swift`, uncomment for more verbose output:

```swift
init() {
    print("Spectra Logger iOS Example Started")
    print("Version: 1.0.0")
    // Add your debugging logs here
}
```

#### Check Pod Installation

```bash
# Verify pods are properly installed
cat Podfile.lock | grep -A 2 "PODS:"

# Check specific framework
ls -la Pods/SpectraLogger/
ls -la Pods/SpectraLoggerUI/
```

### Performance Testing

The example app is optimized to show:
- **Log Capture**: < 1ms per log
- **UI Rendering**: Smooth 60 FPS scrolling with 1000+ logs
- **Memory**: Efficient circular buffer with bounded memory
- **Search/Filter**: Sub-millisecond filtering

Test this yourself:
1. Generate 100+ logs by repeatedly tapping buttons
2. Scroll the log list - should remain smooth
3. Search for logs - filtering is instant
4. No memory warnings or crashes

### Troubleshooting

#### Build Fails with "Module Not Found"

```bash
# 1. Make sure using .xcworkspace, not .xcodeproj
open SpectraExample.xcworkspace

# 2. Clean and rebuild
Xcode: Product → Clean Build Folder (Cmd+Shift+K)
Xcode: Product → Build (Cmd+B)

# 3. If still failing, reset everything
rm -rf Pods Podfile.lock ~/Library/Developer/Xcode/DerivedData/*
./setup-pods.sh
open SpectraExample.xcworkspace
```

#### Framework Not Found Error

```bash
# Rebuild the KMP framework
cd ../..
./gradlew shared:linkReleaseFrameworkIosSimulatorArm64
cd examples/ios-native

# Update pods
pod install
```

#### Simulator Crashes

```bash
# Kill stuck simulator
xcrun simctl shutdown all
xcrun simctl erase all

# Reopen and rebuild
open SpectraExample.xcworkspace
# Cmd+B then Cmd+R
```

#### CocoaPods Issues

```bash
# Update CocoaPods itself
sudo gem install cocoapods

# Clear pod cache
rm -rf ~/Library/Caches/CocoaPods

# Reinstall pods
rm -rf Pods Podfile.lock
./setup-pods.sh
```

## Notes

- The framework must be rebuilt after any changes to the shared module
- Always use `.xcworkspace` (not `.xcodeproj`) when using CocoaPods
- Use Xcode's simulator for development, physical device for testing production builds
- Network logging requires additional URLSession configuration
- All log levels are written with full word names (Verbose, Debug, Info, Warning, Error, Fatal)
- CocoaPods automatically builds the KMP framework via the Podfile's `prepare_command`

## Next Steps

1. Add network logging to your API calls
2. Customize the UI to match your app's design
3. Implement export functionality
4. Add crashlytics or analytics integration
5. Create custom log filters and search
6. Add automatic log rotation to disk storage
7. Create unit tests for your logging integration
