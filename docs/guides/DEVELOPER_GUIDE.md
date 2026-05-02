# Spectra Logger: Developer Onboarding & Environment Guide

This guide ensures a consistent development environment for Spectra across all machines (local development, CI, and new teammates).

---

## 1. Prerequisites

### System Requirements
- **OS**: macOS (Required for iOS development/testing)
- **Xcode**: 15.0+
- **Android Studio**: Ladybug or newer
- **JDK**: 17

---

## 2. Environment Setup

### 2.1 Java Toolchain (Standard)
Spectra uses **Gradle Toolchains** to automatically locate and use the correct JDK. You do **not** need to manually set `org.gradle.java.home` in your `gradle.properties`.

Gradle will search your system for a compatible JDK 17. If one is not found, it will attempt to download one from a standard provider (Adoptium, etc.).

### 2.2 SDKMAN (Recommended)
If you use [SDKMAN](https://sdkman.io/), we provide a `.sdkmanrc` file in the root directory. To switch to the project's Java version automatically, run:

```bash
sdk env
```

> **Current Version**: `17.0.10-amzn` (Amazon Corretto)

---

## 3. Project Structure

Spectra is a Kotlin Multiplatform monorepo:

- `/spectra-core`: Business logic, storage, and platform interceptors.
- `/spectra-ui`: Unified viewer UI built with Compose Multiplatform.
- `/examples`: Native test apps for iOS and Android.

---

## 4. Build & Run

### Common Gradle Commands
| Command | Description |
| :--- | :--- |
| `./gradlew build` | Full project build |
| `./gradlew compileCommonMainKotlinMetadata` | Fast check for shared code errors |
| `./gradlew :spectra-core:allTests` | Run all shared core tests |
| `./gradlew :spectra-ui:connectedAndroidTest` | Run UI tests on Android |

### iOS Binary Generation
To generate the XCFrameworks used by the iOS example app:
```bash
./gradlew :spectra-core:assembleReleaseXCFramework
./gradlew :spectra-ui:assembleReleaseXCFramework
```

---

## 5. Troubleshooting

### "exec: 'npx': executable file not found"
Ensure Node.js is installed on your system. Spectra uses `npx` for some internal design token sync tasks.

### iOS Example App Crashes
If the iOS app crashes with a "missing Info.plist key" error, ensure you have added the ProMotion flag:
`CADisableMinimumFrameDurationOnPhone = YES`

### Gradle Build Fails with machine-specific paths
If you see an error about a path containing another user's name (e.g., `/Users/previous_dev/...`), check your `gradle.properties` or `local.properties` and remove any `org.gradle.java.home` entries. The Toolchain should handle this automatically.
