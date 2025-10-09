# Installation Guide

SpectraLogger supports **two installation methods**: Swift Package Manager (SPM) and CocoaPods.

## Quick Comparison

| Method | Best For | Installation Time | Xcode Integration |
|--------|----------|-------------------|-------------------|
| **SPM** | New projects, Swift-first teams | ⚡ Fast | ✅ Built-in |
| **CocoaPods** | Legacy projects, established teams | ⏱️ Moderate | ⚠️ Requires setup |

**Recommendation:** Use SPM if your project supports it. CocoaPods is available for projects that require it.

---

## Option 1: Swift Package Manager (Recommended)

### Via Xcode (Easiest)

1. **Open your project in Xcode**
2. **File → Add Package Dependencies**
3. **Enter URL:**
   ```
   https://github.com/snooky23/Spectra.git
   ```
4. **Select version:** `1.0.0` or "Up to Next Major Version"
5. **Choose products:**
   - ✅ SpectraLogger (Core framework)
   - ✅ SpectraLoggerUI (Optional: Native iOS UI)
6. **Click "Add Package"**

Done! Xcode will download and integrate the framework automatically.

### Via Package.swift

For Swift packages, add to your `Package.swift`:

```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "YourApp",
    platforms: [
        .iOS(.v13)  // v15 if using SpectraLoggerUI
    ],
    dependencies: [
        .package(
            url: "https://github.com/snooky23/Spectra.git",
            from: "1.0.0"
        )
    ],
    targets: [
        .target(
            name: "YourApp",
            dependencies: [
                .product(name: "SpectraLogger", package: "Spectra"),
                // Optional: .product(name: "SpectraLoggerUI", package: "Spectra")
            ]
        )
    ]
)
```

Then run:
```bash
swift package resolve
```

---

## Option 2: CocoaPods

### 1. Install CocoaPods (if needed)

```bash
sudo gem install cocoapods
```

### 2. Create or Update Podfile

```ruby
platform :ios, '15.0'
use_frameworks!

target 'YourApp' do
  # Core logging framework
  pod 'SpectraLogger', '~> 1.0'

  # Optional: Native iOS UI
  pod 'SpectraLoggerUI', '~> 1.0'
end

# Ensure deployment target compatibility
post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'
    end
  end
end
```

### 3. Install

```bash
pod install
```

### 4. Open Workspace

```bash
open YourApp.xcworkspace  # Important: Use .xcworkspace, not .xcodeproj
```

---

## Minimum Requirements

| Component | iOS Version | Swift Version | Xcode Version |
|-----------|-------------|---------------|---------------|
| **SpectraLogger** (Core) | iOS 13.0+ | Swift 5.9+ | Xcode 14.0+ |
| **SpectraLoggerUI** (UI) | iOS 15.0+ | Swift 5.9+ | Xcode 14.0+ |

---

## Basic Usage

### 1. Import Framework

```swift
import SpectraLogger
import SpectraLoggerUI  // If using UI
```

### 2. Initialize (in AppDelegate or App struct)

```swift
import SwiftUI
import SpectraLogger

@main
struct YourApp: App {
    init() {
        // Initialize SpectraLogger
        SpectraLogger.configure(
            maxInMemoryLogs: 1000,
            enableFileLogging: true
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### 3. Log Messages

```swift
import SpectraLogger

class ViewModel {
    private let logger = SpectraLogger.getLogger(category: "MyFeature")

    func doSomething() {
        logger.info("Operation started")
        logger.debug("Processing data...")

        do {
            try performOperation()
            logger.info("Operation completed successfully")
        } catch {
            logger.error("Operation failed", error: error)
        }
    }
}
```

### 4. Show Logs UI (Optional)

```swift
import SwiftUI
import SpectraLoggerUI

struct ContentView: View {
    @State private var showLogs = false

    var body: some View {
        VStack {
            Button("Show Logs") {
                showLogs = true
            }
        }
        .sheet(isPresented: $showLogs) {
            SpectraLoggerView()
        }
    }
}
```

---

## Advanced Configuration

### Full Configuration Example

```swift
import SpectraLogger

SpectraLogger.configure { config in
    // In-memory storage
    config.maxInMemoryLogs = 5000
    config.circularBufferEnabled = true

    // File logging
    config.enableFileLogging = true
    config.maxLogFileSize = 10 * 1024 * 1024  // 10 MB
    config.logFileRotationCount = 3

    // Network logging
    config.enableNetworkLogging = true
    config.maxNetworkBodySize = 100 * 1024  // 100 KB
    config.redactedHeaders = ["Authorization", "Cookie", "Set-Cookie"]
    config.ignoredDomains = ["analytics.google.com", "crashlytics.com"]

    // Log levels
    config.minimumLogLevel = .debug  // .verbose, .debug, .info, .warning, .error
    config.fileLogLevel = .info      // Only log .info and above to file

    // UI settings (if using SpectraLoggerUI)
    config.enableFloatingButton = true  // Show floating action button
    config.enableShakeGesture = true    // Shake to open logs
}
```

### Network Logging Setup

#### For URLSession (Native)

```swift
import SpectraLogger

// Enable automatic network logging
SpectraLogger.registerURLProtocol()

// Use URLSession as normal
let session = URLSession.shared
let task = session.dataTask(with: url) { data, response, error in
    // Requests/responses are automatically logged
}
task.resume()
```

#### For Alamofire

```swift
import Alamofire
import SpectraLogger

let session = Session(interceptor: SpectraNetworkInterceptor())

session.request("https://api.example.com/users")
    .responseJSON { response in
        // Logged automatically
    }
```

#### For OkHttp (Android - if using KMP)

```kotlin
import com.spectra.logger.SpectraLogger
import okhttp3.OkHttpClient

val client = OkHttpClient.Builder()
    .addInterceptor(SpectraLogger.createOkHttpInterceptor())
    .build()
```

---

## Verification

After installation, verify it works:

```swift
import SpectraLogger

// In your app initialization
let logger = SpectraLogger.getLogger(category: "Installation")
logger.info("SpectraLogger installed successfully! 🎉")

// Check version
print("SpectraLogger version: \(SpectraLogger.version)")
```

---

## Troubleshooting

### SPM Issues

**"No such module SpectraLogger"**
1. Clean build folder: `Product → Clean Build Folder`
2. Reset package cache: `File → Packages → Reset Package Caches`
3. Restart Xcode

**"Binary target checksum mismatch"**
1. Check internet connection
2. Verify you're using the correct version
3. Try resetting package caches

### CocoaPods Issues

**"Unable to find a specification for SpectraLogger"**
```bash
pod repo update
pod search SpectraLogger
```

**"The platform... is not compatible"**
- Ensure Podfile has `platform :ios, '15.0'` or higher
- Add post_install script (see CocoaPods section above)

**"Pods not linking correctly"**
- Make sure you're opening `.xcworkspace`, not `.xcodeproj`
- Clean derived data: `Shift+Cmd+K` in Xcode

---

## Migration Between Methods

### From CocoaPods to SPM

1. Remove from Podfile
2. Run `pod deintegrate`
3. Delete `Pods/` directory and `.xcworkspace`
4. Follow SPM installation above
5. Update imports (no changes needed)

### From SPM to CocoaPods

1. Remove package from Xcode (File → Packages)
2. Create Podfile (see CocoaPods section)
3. Run `pod install`
4. Open `.xcworkspace`

---

## Next Steps

- 📖 Read the [Usage Guide](./USAGE_GUIDE.md) for detailed features
- 📖 See [API Documentation](./API.md) for complete reference
- 📖 Check out [Example Projects](../examples/) for sample code
- 🔧 Configure [Network Logging](./COCOAPODS_GUIDE.md#network-logging-setup)
- 🎨 Customize [UI Appearance](./USAGE_GUIDE.md#ui-customization)

---

## Support

- 📘 Documentation: https://github.com/snooky23/Spectra/tree/main/docs
- 🐛 Issues: https://github.com/snooky23/Spectra/issues
- 💬 Discussions: https://github.com/snooky23/Spectra/discussions

---

**Last Updated:** 2025-10-08
**SpectraLogger Version:** 1.0.0
