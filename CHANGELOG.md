# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Batch logging demo in example apps (10/100 logs)
- Real-time background logging demo
- All log levels demo
- Searchable logs demo
- Additional HTTP methods (PUT, DELETE, 429)
- Batch network simulation (10 API calls)

### Changed
- Rewrote README with industry-standard format
- Reorganized project structure (spectra-core, spectra-ui-ios, spectra-ui-android)

### Fixed
- iOS Package.swift path detection for local XCFramework
- NetworkLogEntry API with source/sourceType parameters

---

## [0.0.1] - 2025-12-13

### Added
- Initial release
- Core logging engine with 6 log levels (VERBOSE, DEBUG, INFO, WARNING, ERROR, FATAL)
- Network request/response logging
- OkHttp interceptor for Android
- URLSession logger for iOS
- Ktor plugin for KMP
- On-device log viewer UI (Compose for Android, SwiftUI for iOS)
- Log filtering by level, tag, and search text
- Network log filtering by method, status code, and URL
- Share/Export functionality (JSON, Text formats)
- Dark mode support
- File storage with automatic rotation
- Thread-safe async logging
- Example apps for Android and iOS

### Technical
- Kotlin Multiplatform architecture
- Clean Architecture with MVVM
- XCFramework for iOS distribution
- Gradle multi-module build

---

[Unreleased]: https://github.com/snooky23/spectra/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/snooky23/spectra/releases/tag/v0.0.1
