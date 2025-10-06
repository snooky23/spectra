# Spectra Logger - iOS Native Example

This example demonstrates how to use Spectra Logger in a native iOS application using Swift and SwiftUI.

## Prerequisites

- Xcode 15.0 or later
- iOS 15.0 or later
- macOS with Apple Silicon or Intel processor

## Setup

### Option 1: Using CocoaPods (Recommended for Local Development)

1. **Install CocoaPods** (if not already installed):
   ```bash
   sudo gem install cocoapods
   ```

2. **Set up CocoaPods and build the framework**:
   ```bash
   cd examples/ios-native
   ./setup-pods.sh
   ```

3. **Open the workspace** (NOT the .xcodeproj):
   ```bash
   open SpectraExample.xcworkspace
   ```

4. **Build and run the project**:
   - Select iPhone simulator (iPhone 15 Pro recommended)
   - Press Cmd+R to build and run

5. **After making changes to the shared module**:
   ```bash
   pod update SpectraLogger
   ```

### Option 2: Direct Framework Linking (Alternative)

1. **Build the Spectra framework**:
   ```bash
   cd ../..
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```

2. **Create the Xcode project** (first time only):
   ```bash
   cd examples/ios-native
   ./create-xcode-project.sh
   ```

3. **Open the Xcode project**:
   ```bash
   open SpectraExample.xcodeproj
   ```

4. **Build and run the project**:
   - Select iPhone simulator (iPhone 15 Pro recommended)
   - Press Cmd+R to build and run

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
├── SpectraExample.xcodeproj/          # Xcode project file
├── SpectraExample.xcworkspace/        # Workspace (when using CocoaPods)
├── SpectraExample/                    # Main application directory
│   └── SpectraExample/                # Source files
│       ├── SpectraExampleApp.swift   # App entry point with URL scheme handling
│       ├── MainAppView.swift         # Main app screen with Spectra button
│       ├── ContentView.swift         # Spectra Logger UI views
│       └── Assets.xcassets/          # App icons and colors
├── Podfile                            # CocoaPods dependencies
├── setup-pods.sh                      # CocoaPods setup script
├── add-url-scheme.sh                  # URL scheme configuration helper
├── create-xcode-project.sh            # Script to generate Xcode project
└── README.md                          # This file
```

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

## Notes

- The framework must be rebuilt after any changes to the shared module
- Use Xcode's simulator for development, physical device for testing production builds
- Network logging requires additional URLSession configuration
- All log levels are written with full word names (Verbose, Debug, Info, Warning, Error, Fatal)

## Next Steps

1. Customize the UI to match your app's design
2. Add network logging for your API calls
3. Implement export functionality
4. Add crashlytics or analytics integration
5. Create custom log filters and search
