# Spectra Logger - KMP Example App

A complete Kotlin Multiplatform application demonstrating Spectra Logger usage with fully shared UI code.

## Overview

This example shows how to use Spectra Logger in a KMP project where:
- **Shared Module**: Contains all business logic and UI (Compose Multiplatform)
- **Android App**: Thin wrapper around shared module
- **iOS App**: Thin wrapper around shared module

## Features

- ✅ Fully shared Compose UI across iOS and Android
- ✅ Shared logging logic
- ✅ Shared network logging
- ✅ Platform-specific integrations where needed

## Project Structure

```
kmp-app/
├── shared/                    # Shared KMP module
│   ├── commonMain/
│   │   └── App.kt            # Main app composable
│   ├── androidMain/          # Android-specific code
│   └── iosMain/              # iOS-specific code
├── androidApp/               # Android application
│   └── MainActivity.kt
└── iosApp/                   # iOS application
    └── iOSApp.swift
```

## Building

### Android
```bash
./gradlew :examples:kmp-app:androidApp:assembleDebug
```

### iOS
```bash
./gradlew :examples:kmp-app:shared:linkDebugFrameworkIosSimulatorArm64
open examples/kmp-app/iosApp/iosApp.xcodeproj
```

## Key Differences from Native Examples

1. **Shared UI**: All screens are in `shared/commonMain` using Compose Multiplatform
2. **Single Codebase**: Write once, run on both platforms
3. **Platform Integration**: Use `expect`/`actual` for platform-specific features

## Learning Resources

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Spectra Logger Documentation](../../README.md)
