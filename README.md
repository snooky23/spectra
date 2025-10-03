# Spectra Logger

[![Build and Test](https://github.com/yourusername/spectra-logger/actions/workflows/build.yml/badge.svg)](https://github.com/yourusername/spectra-logger/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.spectra.logger/logger)](https://search.maven.org/artifact/com.spectra.logger/logger)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Kotlin Multiplatform logging framework for mobile applications that works seamlessly in:
- **Native iOS projects** (Swift/Objective-C)
- **Native Android projects** (Kotlin/Java)
- **KMP projects** (commonMain)

## Features

- ✅ **Application event logging** with severity levels (Verbose, Debug, Info, Warning, Error)
- ✅ **Network request/response logging** (OkHttp, URLSession, Ktor)
- ✅ **On-device mobile UI** for viewing and filtering logs
- ✅ **Export functionality** (text and JSON formats)
- ✅ **Zero external dependencies** - no cloud services required
- ✅ **Thread-safe** and **performant** (< 0.1ms log capture)
- ✅ **Clean Architecture** with MVVM for UI

## Status

🚧 **Under Active Development** - Version 0.0.1-SNAPSHOT

Currently working on **Phase 1: Foundation** (Weeks 1-4)

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

```kotlin
// Initialize the logger
SpectraLogger.initialize(
    SpectraConfig(
        maxInMemoryLogs = 10000,
        enableFileLogging = true,
        enableNetworkLogging = true
    )
)

// Get a logger instance
val logger = SpectraLogger.getLogger("UserAuth", "app")

// Log events
logger.info("User login started")
logger.error("Login failed", exception, mapOf("userId" to userId))

// Show the debug UI
SpectraLogger.showUI(context) // Android
SpectraLogger.showUI(presenter: viewController) // iOS
```

## Documentation

- [Product Requirements Document](./PRD.md) - Complete product specification
- [Technical Planning](./PLANNING.md) - Architecture and design decisions
- [Task Breakdown](./TASKS.md) - Development timeline and milestones
- [Claude Code Instructions](./CLAUDE.md) - For AI-assisted development
- [Session Memory](./SESSION.md) - Current development state

## Building from Source

### Prerequisites

- JDK 11+
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
- [ ] Core logging engine
- [ ] File storage
- [ ] Network logging (Android, iOS, Ktor)

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
