# Spectra Logger - iOS Native Example

This example demonstrates how to use Spectra Logger in a native iOS application using Swift and SwiftUI.

## Prerequisites

- Xcode 15.0 or later
- iOS 14.0 or later
- CocoaPods (for dependency management)

## Setup

1. Build the Spectra framework:
   ```bash
   cd ../..
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```

2. Open the Xcode project:
   ```bash
   open SpectraExample.xcodeproj
   ```

3. Build and run the project

## Features Demonstrated

- **Logging**: All log levels (Verbose, Debug, Info, Warning, Error, Fatal)
- **Network Logging**: Automatic network request/response capture
- **UI Components**:
  - Log list screen with filtering
  - Network logs screen
  - Settings screen
- **Configuration**: Runtime logger configuration
- **Export**: Share logs functionality

## Integration

The example shows how to:
1. Link the Kotlin framework to Swift
2. Initialize Spectra Logger
3. Use the logger in Swift code
4. Display Compose UI from Swift

## Project Structure

```
SpectraExample/
├── SpectraExample/
│   ├── SpectraExampleApp.swift    # App entry point
│   ├── ContentView.swift           # Main UI
│   ├── LoggerBridge.swift         # Kotlin-Swift bridge
│   └── Info.plist
└── Frameworks/
    └── shared.framework            # Linked from ../../shared/build/
```

## Notes

- The framework must be rebuilt after any changes to the shared module
- Use Xcode's simulator or a physical device for testing
- Network logging works automatically when using URLSession
