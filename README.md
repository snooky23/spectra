# Spectra Logger

[![Build and Test](https://github.com/snooky23/spectra/actions/workflows/build.yml/badge.svg)](https://github.com/snooky23/spectra/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/snooky23/Spectra/branch/main/graph/badge.svg)](https://codecov.io/gh/snooky23/Spectra)
[![Maven Central](https://img.shields.io/maven-central/v/com.spectra.logger/logger)](https://search.maven.org/artifact/com.spectra.logger/logger)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Kotlin Multiplatform logging framework for mobile applications that works seamlessly in:
- **Native iOS projects** (Swift/Objective-C)
- **Native Android projects** (Kotlin/Java)
- **KMP projects** (commonMain)

## Features

### Core Logging
- ✅ **Application event logging** with severity levels (Verbose, Debug, Info, Warning, Error, Fatal)
- ✅ **Network request/response logging** (OkHttp, URLSession, Ktor)
- ✅ **Unique message IDs** for tracking individual log entries
- ✅ **Stack trace capture** for errors and exceptions
- ✅ **Metadata support** for additional context on every log

### Application Context
- ✅ **AppContext model** - Capture app info once, enrich all logs automatically
- ✅ **Build/Version info** - Version, build number for reproduction
- ✅ **Device information** - Model, OS version for environment-specific bugs
- ✅ **Session tracking** - Group related logs within a session
- ✅ **User identification** - Optional user ID for user-specific debugging

### UI & Filtering
- ✅ **On-device mobile UI** for viewing and filtering logs
- ✅ **Multi-level filtering** - By log level, tag, and search text
- ✅ **Tag-based grouping** - Organize logs by component/source
- ✅ **Expandable error sections** - View full stack traces with line numbers
- ✅ **Share native buttons** - Export logs via iOS share sheet
- ✅ **Dark mode support** - Light/Dark/System appearance modes

### Advanced Features
- ✅ **Multiple trigger options** - Shake gesture, notification, URL scheme, programmatic
- ✅ **Export & share functionality** (TEXT, JSON formats)
- ✅ **URL scheme deep linking** - Open logger via `spectra://` from ADB, browser, or terminal
- ✅ **Zero permissions required** - no cloud services, no invasive permissions
- ✅ **Thread-safe** and **performant** (< 0.1ms log capture)
- ✅ **Clean Architecture** with MVVM for UI

## Status

🚧 **Under Active Development** - Version 0.0.1-SNAPSHOT

**Completed:**
- ✅ Milestone 1.1: Project Foundation Setup
- ✅ Milestone 1.2: Core Domain Layer
- ✅ Milestone 1.3: Network Logging Foundation
- ✅ Milestone 1.4: Configuration System
- ✅ Milestone 2.1: Basic UI Components
- ✅ Milestone 2.2: Enhanced Features (Shake, Notification, Export, URL Scheme)
- ✅ Milestone 2.3: File Storage & Persistence (with automatic rotation)
- ✅ Milestone 2.4: UI Enhancements (Detail view, Search functionality)

**Next:** Milestone 3 - Advanced Features & Polish

See [TASKS.md](./TASKS.md) for detailed development timeline.

## Quick Start

### For KMP Projects

```kotlin
// In shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.spectra:logger:0.0.1-SNAPSHOT")
        }
    }
}
```

### For Native Android

```kotlin
// In build.gradle
dependencies {
    implementation("com.spectra:logger-android:0.0.1-SNAPSHOT")
}
```

### For Native iOS

```ruby
# In Podfile
pod 'SpectraLogger', '~> 0.0.1'
```

## Usage

### Core Logging Functions

Spectra Logger provides a consistent, simple API for logging across all platforms, following industry standards from Firebase Crashlytics, Sentry, and Log4j.

```kotlin
import com.spectra.logger.SpectraLogger

// Simple logging (metadata is optional)
SpectraLogger.v("TAG", "Verbose message")
SpectraLogger.d("TAG", "Debug message")
SpectraLogger.i("TAG", "Info message")
SpectraLogger.w("TAG", "Warning message")
SpectraLogger.e("TAG", "Error message", throwable = exception)
SpectraLogger.f("TAG", "Fatal error")

// Add metadata/context (nullable - industry standard)
SpectraLogger.d("Auth", "Login attempt", metadata = mapOf(
    "username" to "user@example.com",
    "deviceId" to "ABC123"
))

// Or explicitly pass null (equivalent to no metadata)
SpectraLogger.d("TAG", "message", metadata = null)

// Metadata is optional - can be omitted entirely
SpectraLogger.d("TAG", "message")
```

**Function Naming Convention:**
- `v()` - Verbose (lowest priority)
- `d()` - Debug
- `i()` - Info
- `w()` - Warning
- `e()` - Error
- `f()` - Fatal (highest priority)

**All functions accept:**
- `tag: String` - Category/source of the log
- `message: String` - The log message
- `throwable: Throwable?` - Optional exception for error logs
- `metadata: Map<String, String>?` - Optional context data *(nullable, follows industry standard)*

**Design Note:** The metadata parameter follows the industry standard pattern used by Firebase Crashlytics, Sentry, and Log4j, where context data is optional and nullable. Null values are automatically converted to empty maps internally, matching the behavior of professional logging frameworks.

### Configuration

```kotlin
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogLevel

// Configure before logging (optional - uses sensible defaults)
SpectraLogger.configure {
    minLogLevel = LogLevel.DEBUG  // Filter out verbose logs

    logStorage {
        maxCapacity = 20_000  // Increase from default 10K
    }

    networkStorage {
        maxCapacity = 2_000   // Increase from default 1K
    }

    performance {
        flowBufferCapacity = 128
        maxBodySize = 50_000  // Max network body size
    }

    features {
        enableNetworkLogging = true
        enableCrashReporting = false
    }
}
```

### Network Logging

#### Android (OkHttp)

```kotlin
import com.spectra.logger.SpectraLogger
import com.spectra.logger.network.SpectraNetworkInterceptor

val client = OkHttpClient.Builder()
    .addInterceptor(SpectraNetworkInterceptor(SpectraLogger.networkStorage))
    .build()
```

#### KMP (Ktor)

```kotlin
import com.spectra.logger.network.SpectraNetworkLogger
import com.spectra.logger.SpectraLogger

val client = HttpClient {
    install(SpectraNetworkLogger) {
        storage = SpectraLogger.networkStorage
    }
}
```

#### iOS (URLSession)

**Option 1: Manual Logging (Recommended)**
```swift
import SpectraLogger

// Create a logging data task
let storage = SpectraLogger.shared.networkStorage
let session = NSURLSession.shared
let request = NSURLRequest(url: url)

let task = SpectraURLSessionLogger.createDataTask(
    session: session,
    request: request,
    storage: storage
) { data, response, error in
    // Your completion handler
}
task.resume()
```

**Option 2: Use Ktor in Shared Code (Automatic)**
See KMP section above for automatic network logging.

### File Storage & Persistence

Spectra Logger supports persistent file storage with automatic rotation to prevent unlimited disk usage.

#### Android

```kotlin
import com.spectra.logger.domain.storage.FileLogStorage
import com.spectra.logger.storage.FileSystem

// Create file storage with automatic rotation
val fileStorage = FileLogStorage(
    fileSystem = FileSystem(context),
    maxFileSize = 1_048_576L,  // 1MB per file (default)
    maxFiles = 5                // Keep max 5 files (default)
)

// Initialize to find existing log files
fileStorage.initialize()

// Use as your log storage
SpectraLogger.configure {
    logStorage = fileStorage
}
```

#### iOS

```swift
import SpectraLogger

// Create file storage with automatic rotation
let fileStorage = FileLogStorage(
    fileSystem: FileSystem(),
    maxFileSize: 1_048_576,  // 1MB per file
    maxFiles: 5               // Keep max 5 files
)

// Initialize to find existing log files
fileStorage.initialize()

// Configure logger
SpectraLogger.shared.configure { config in
    config.logStorage = fileStorage
}
```

**Features:**
- Automatic file rotation when size limit is reached
- Oldest files deleted when max file count exceeded
- JSONL format (one JSON object per line) for efficient streaming
- Persists across app restarts
- Platform-specific storage:
  - Android: `Context.filesDir/spectra_logs/`
  - iOS: `NSDocumentDirectory/spectra_logs/`

### Show Logger UI

The Spectra Logger UI is accessed through a simple, consistent API across all platforms.

#### Basic Usage (All Platforms)

```kotlin
import com.spectra.logger.SpectraLogger

// Show the logger UI as a modal
SpectraLogger.showScreen()

// Dismiss the logger UI
SpectraLogger.dismissScreen()
```

#### Android - Trigger Options

**Option 1: From Activity/Fragment**
```kotlin
// On button click
button.setOnClickListener {
    SpectraLogger.showScreen()
}

// From Fragment
button.setOnClickListener {
    SpectraLogger.showScreen()
}
```

**Option 2: Shake Gesture (Automatic)**
```kotlin
import com.spectra.logger.ui.SpectraLoggerTrigger

// In your Application class
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable shake-to-open logger
        SpectraLoggerTrigger.enableShakeToOpen(this)
    }
}
```

**Option 3: Persistent Notification**
```kotlin
import com.spectra.logger.ui.SpectraLoggerNotification

// Show notification (taps opens logger)
SpectraLoggerNotification.show(context, "Debug Mode Active")

// Hide notification
SpectraLoggerNotification.hide(context)
```

**Option 4: URL Scheme**
```bash
# Android - via ADB
adb shell am start -a android.intent.action.VIEW -d "spectra://open"

# From browser or webview
# Simply navigate to: spectra://open

# From Chrome DevTools remote debugging
window.location = "spectra://open"
```

#### iOS - Trigger Options

**Option 1: From SwiftUI Button**
```swift
import SpectraLogger

Button("Show Logs") {
    SpectraLogger.showScreen()
}
```

**Option 2: From UIKit**
```swift
import SpectraLogger

button.addTarget(self, action: #selector(showLogs), for: .touchUpInside)

@objc func showLogs() {
    SpectraLogger.showScreen()
}
```

**Option 3: Shake Gesture**
```swift
@main
struct MyApp: App {
    init() {
        // Enable shake-to-open logger
        SpectraLogger.enableShakeGesture()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

**Option 4: URL Scheme**

1. Add to `Info.plist`:
```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>spectra</string>
        </array>
    </dict>
</array>
```

2. Handle in SwiftUI:
```swift
@main
struct MyApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    if url.scheme == "spectra" {
                        SpectraLogger.showScreen()
                    }
                }
        }
    }
}
```

3. Open from terminal:
```bash
# iOS Simulator
xcrun simctl openurl booted "spectra://open"

# Or from Safari - just type: spectra://open
```

### Export and Share Logs

```kotlin
import com.spectra.logger.export.FileExporter
import com.spectra.logger.export.ExportFormat

// Export to file
val file = FileExporter.exportLogsToFile(
    context = context,
    format = ExportFormat.JSON,
    filter = LogFilter(levels = setOf(LogLevel.ERROR))
)

// Share via Android share sheet
FileExporter.shareLogs(
    context = context,
    format = ExportFormat.TEXT
)

// Share network logs
FileExporter.shareNetworkLogs(
    context = context,
    format = ExportFormat.JSON
)
```

### Querying Logs Programmatically

```kotlin
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.LogLevel

// Query logs
val logs = SpectraLogger.query(
    filter = LogFilter(
        levels = setOf(LogLevel.ERROR, LogLevel.FATAL),
        tags = setOf("Network", "Database"),
        searchText = "timeout"
    ),
    limit = 100
)

// Observe logs in real-time
SpectraLogger.observe().collect { logEntry ->
    println("New log: ${logEntry.message}")
}

// Query network logs
val networkLogs = SpectraLogger.queryNetwork(
    filter = NetworkLogFilter(
        methods = setOf("POST"),
        statusCodes = setOf(500, 503),
        showOnlyErrors = true
    )
)
```

## Testing

### Quick Start Testing

```bash
# Test the iOS example app (recommended for visual testing)
cd examples/ios-native
./setup-pods.sh
open SpectraExample.xcworkspace
# Then press Cmd+R to build and run

# Test the Swift package only
cd SpectraLoggerUI
swift test

# Test the Kotlin core
./gradlew test
```

For detailed testing guide with troubleshooting, see:
- **[iOS Testing Guide](./iOS_TESTING_GUIDE.md)** - Quick reference for all iOS testing
- **[iOS Example App README](./examples/ios-native/README.md#testing)** - How to test the example app
- **[SpectraLoggerUI Testing Guide](./SpectraLoggerUI/TESTING.md)** - Comprehensive testing guide

## Documentation

- [Product Requirements Document](./PRD.md) - Complete product specification
- [Technical Planning](./PLANNING.md) - Architecture and design decisions
- [Task Breakdown](./TASKS.md) - Development timeline and milestones
- [Claude Code Instructions](./CLAUDE.md) - For AI-assisted development
- [Session Memory](./SESSION.md) - Current development state
- [iOS Testing Guide](./iOS_TESTING_GUIDE.md) - Quick reference for iOS testing
- [SpectraLoggerUI Documentation](./SpectraLoggerUI/README.md) - SwiftUI package guide
- [API Reference](./docs/API.md) - Complete API documentation
- [Usage Guide](./docs/USAGE_GUIDE.md) - How to use Spectra Logger

## Building from Source

### Prerequisites

- JDK 17+ (recommend installing via [SDKMAN](https://sdkman.io/))
- Android Studio Hedgehog or later (for Android)
- Xcode 15+ (for iOS)
- macOS 13+ (for iOS development)

### Build

```bash
# Clone the repository
git clone https://github.com/yourusername/spectra-logger.git
cd spectra-logger

# Build all modules
./gradlew build

# Run tests
./gradlew test

# Run code quality checks
./gradlew ktlintCheck detekt
```

## Project Structure

```
Spectra/
├── shared/                 # Core KMP module
│   ├── src/
│   │   ├── commonMain/    # Shared business logic
│   │   ├── androidMain/   # Android-specific code
│   │   └── iosMain/       # iOS-specific code
├── examples/              # Example applications
│   ├── android-native/   # Native Android example
│   ├── ios-native/       # Native iOS example
│   └── kmp-app/          # KMP example
├── docs/                  # Documentation
└── config/                # Build configuration
```

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our code of conduct and development process.

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (conventional commits format)
4. Push to the branch
5. Open a Pull Request

See [CLAUDE.md](./CLAUDE.md) for detailed coding standards and architecture guidelines.

### Local Development Scripts

The `scripts/` directory contains standalone shell scripts that replicate the GitHub Actions CI/CD workflows, allowing you to run them locally before pushing:

```bash
# Run full CI pipeline (recommended before pushing)
./scripts/ci.sh

# Individual scripts
./scripts/build.sh          # Build the project
./scripts/test.sh           # Run all tests
./scripts/code-quality.sh   # Run ktlint + detekt
./scripts/coverage.sh       # Generate coverage reports
./scripts/format.sh         # Auto-format code with ktlint
./scripts/clean.sh          # Clean build artifacts
```

**Benefits:**
- ✅ Test changes before pushing to GitHub
- ✅ Fast feedback loop during development
- ✅ Same checks as GitHub Actions CI
- ✅ Easy migration to other CI platforms (CircleCI, Jenkins, etc.)

See [scripts/README.md](./scripts/README.md) for detailed documentation.

## Roadmap

### Version 0.1.0 (Phase 1-2) - Foundation
- [x] Project setup and CI/CD
- [x] Core logging engine
- [x] Configuration system
- [x] Network logging (OkHttp, URLProtocol, Ktor)
- [x] Basic UI components
- [x] File storage with automatic rotation
- [x] Enhanced features (shake, notification, export, URL scheme)
- [x] UI enhancements (detail view, search)

### Version 0.5.0 (Phase 3) - UI
- [ ] Log viewer screen
- [ ] Network viewer screen
- [ ] Settings screen
- [ ] Export functionality

### Version 1.0.0 (Phase 4-5) - Production Ready
- [ ] Complete documentation
- [ ] Example applications
- [ ] Performance testing
- [ ] Beta testing
- [ ] Official release

See [TASKS.md](./TASKS.md) for complete 18-week development plan.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to the Kotlin Multiplatform community
- Built with modern Android and iOS development best practices

## Support

- 🐛 [Report a bug](https://github.com/yourusername/spectra-logger/issues/new?template=bug_report.md)
- 💡 [Request a feature](https://github.com/yourusername/spectra-logger/issues/new?template=feature_request.md)
- 💬 [Discussions](https://github.com/yourusername/spectra-logger/discussions)

---

**Built with ❤️ using Kotlin Multiplatform**
