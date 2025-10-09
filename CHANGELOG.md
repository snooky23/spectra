# Changelog

All notable changes to SpectraLogger will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Android native UI companion library
- Performance benchmarks and profiling tools
- Log export in additional formats (HTML, Markdown)

---

## [1.0.0] - 2025-10-09

### 🎉 First Production Release

The first stable release of SpectraLogger - a high-performance, cross-platform logging framework for iOS and Android.

### Added

#### Core Framework
- **Ultra-fast logging** with < 0.1ms capture time (99th percentile < 1ms)
- **Circular buffer storage** with automatic pruning to prevent unbounded memory growth
- **Thread-safe operations** optimized for multi-threaded mobile applications
- **Multiple log levels**: Verbose, Debug, Info, Warning, Error
- **Structured logging** with tags, metadata, and exception tracking
- **Network request/response interception** for automatic HTTP/HTTPS traffic logging
- **File-based persistence** with configurable rotation and size limits
- **Cross-platform support**: iOS 13.0+ and Android 7.0+ (API 24+)

#### iOS UI Companion (SpectraLoggerUI)
- **Native SwiftUI log viewer** with Material Design-inspired interface
- **Real-time log streaming** with smooth 60 FPS scrolling performance
- **Advanced filtering** by log level, tag, time range, and search query
- **Network traffic inspector** with JSON syntax highlighting
- **cURL command generation** for reproducing network requests
- **Multiple export formats**: JSON, Plain Text, CSV
- **AirDrop integration** for easy log sharing
- **Full accessibility support** with VoiceOver and Dynamic Type

#### Distribution
- **Swift Package Manager** support with binary XCFramework distribution
- **CocoaPods** support (both source and binary distribution)
- **Carthage** support with binary JSON manifest
- **Lightweight**: Only 8.5MB XCFramework (93% smaller than beta versions)

#### Documentation
- Comprehensive installation guide for all three package managers
- Platform-specific guides (CocoaPods, SPM, Carthage)
- Architecture documentation
- API reference with code examples
- CI/CD setup guide for maintainers

#### Developer Experience
- **Automated CI/CD** with GitHub Actions
- Code quality checks (ktlint, detekt)
- Test coverage reporting with Jacoco
- Automated releases with GitHub Actions

### Architecture

- **Pure Kotlin Multiplatform core** with zero UI dependencies
- **Separate UI layers** for each platform (iOS: SwiftUI, Android: Jetpack Compose)
- **Clean separation of concerns** between logging engine and UI presentation
- **Platform-specific optimizations** using expect/actual pattern

### Technical Specifications

- **Platforms**: iOS 13.0+, Android 7.0+ (API 24+)
- **Languages**: Kotlin Multiplatform (core), Swift/SwiftUI (iOS UI)
- **Size**: 8.5MB XCFramework (device: 11MB, simulator: 21MB)
- **Dependencies**: Zero external dependencies in core framework
- **License**: Apache 2.0 (commercial-friendly)

### Breaking Changes

This is the first stable release, so there are no breaking changes. However, compared to beta versions:
- **Removed**: Compose Multiplatform UI from shared module (moved to platform-specific modules)
- **Changed**: UI is now distributed as separate optional package (SpectraLoggerUI for iOS)

### Migration from Beta

If you were using a beta version with Compose UI in the shared module:
1. Update to `SpectraLogger ~> 1.0` (core logging only)
2. Add `SpectraLoggerUI ~> 1.0` for iOS UI (optional)
3. Update imports: `import SpectraLogger` (core) and `import SpectraLoggerUI` (UI)

---

## [0.x.x] - Beta Releases

Beta versions (0.1.0 - 0.9.x) were used for internal testing and proof of concept. These versions are not recommended for production use.

### Key Changes from Beta to 1.0.0
- Reduced XCFramework size by 93% (119MB → 8.5MB)
- Separated UI from core framework
- Added comprehensive documentation
- Established CI/CD pipeline
- Added support for all major iOS package managers

---

## Release Notes

### How to Update

#### Swift Package Manager
```swift
dependencies: [
    .package(url: "https://github.com/snooky23/Spectra.git", from: "1.0.0")
]
```

#### CocoaPods
```ruby
pod 'SpectraLogger', '~> 1.0'
pod 'SpectraLoggerUI', '~> 1.0'  # Optional
```

#### Carthage
```ruby
binary "https://raw.githubusercontent.com/snooky23/Spectra/main/SpectraLogger.json" ~> 1.0
```

### Support

- 📘 Documentation: https://github.com/snooky23/Spectra/tree/main/docs
- 🐛 Issues: https://github.com/snooky23/Spectra/issues
- 💬 Discussions: https://github.com/snooky23/Spectra/discussions

---

[Unreleased]: https://github.com/snooky23/Spectra/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/snooky23/Spectra/releases/tag/v1.0.0
