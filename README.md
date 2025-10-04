# Spectra Logger

[![Build and Test](https://github.com/yourusername/spectra-logger/actions/workflows/build.yml/badge.svg)](https://github.com/yourusername/spectra-logger/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.spectra.logger/logger)](https://search.maven.org/artifact/com.spectra.logger/logger)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Kotlin Multiplatform logging framework for mobile applications that works seamlessly in:
- **Native iOS projects** (Swift/Objective-C)
- **Native Android projects** (Kotlin/Java)
- **KMP projects** (commonMain)

## Features

- ✅ **Application event logging** with severity levels (Verbose, Debug, Info, Warning, Error, Fatal)
- ✅ **Network request/response logging** (OkHttp, URLSession, Ktor)
- ✅ **On-device mobile UI** for viewing and filtering logs
- ✅ **Multiple trigger options** - Shake gesture, notification, URL scheme, programmatic
- ✅ **Export & share functionality** (TEXT, JSON, CSV formats)
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

**Next:** Milestone 2.3 - File Storage & Persistence

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

### Basic Logging

```kotlin
import com.spectra.logger.SpectraLogger

// Log events (available globally)
SpectraLogger.v("TAG", "Verbose message")
SpectraLogger.d("TAG", "Debug message")
SpectraLogger.i("TAG", "Info message")
SpectraLogger.w("TAG", "Warning message")
SpectraLogger.e("TAG", "Error message", throwable = exception)
SpectraLogger.f("TAG", "Fatal error", metadata = mapOf("userId" to "123"))
```

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

### Show Logger UI

#### Android

**Option 1: Manual**
```kotlin
import com.spectra.logger.ui.SpectraLoggerUI

// From any Activity or Context
SpectraLoggerUI.show(context)
```

**Option 2: Shake Gesture**
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

**Option 4: URL Scheme** (Works from ADB, browser, or other apps)
```bash
# Android - via ADB
adb shell am start -a android.intent.action.VIEW -d "spectra://open"
adb shell am start -a android.intent.action.VIEW -d "spectra://logs"
adb shell am start -a android.intent.action.VIEW -d "spectra://network"

# Android - from browser or webview
# Simply navigate to: spectra://open

# From Chrome DevTools remote debugging
window.location = "spectra://network"
```

#### iOS

**URL Scheme Setup:**

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
                        // Present logger UI
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

## Documentation

- [Product Requirements Document](./PRD.md) - Complete product specification
- [Technical Planning](./PLANNING.md) - Architecture and design decisions
- [Task Breakdown](./TASKS.md) - Development timeline and milestones
- [Claude Code Instructions](./CLAUDE.md) - For AI-assisted development
- [Session Memory](./SESSION.md) - Current development state

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

## Roadmap

### Version 0.1.0 (Phase 1-2) - Foundation
- [x] Project setup and CI/CD
- [x] Core logging engine
- [x] Configuration system
- [x] Network logging (OkHttp, URLProtocol, Ktor)
- [x] Basic UI components
- [ ] File storage
- [ ] Enhanced features

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
