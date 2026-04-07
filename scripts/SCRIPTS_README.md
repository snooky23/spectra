# Spectra Logger Scripts

This directory contains all build, test, and CI/CD scripts for the Spectra Logger project.

## Directory Structure

```
scripts/
├── build/          # Build scripts for all platforms
├── test/           # Test scripts
├── setup/          # Development environment setup
├── release/        # Release automation
├── ci/             # CI/CD utility scripts
└── README.md       # This file
```

## Quick Start

### First Time Setup

```bash
# Set up development environment
./scripts/setup/setup-dev.sh
```

### Build Everything

```bash
# Build all platforms (KMP + iOS + Android)
./scripts/build/build-all.sh
```

### Run All Tests

```bash
# Run tests for all platforms
./scripts/test/test-all.sh
```

---

## Build Scripts (`build/`)

### `build-kmp.sh`

Builds the Kotlin Multiplatform shared modules for all platforms.

```bash
./scripts/build/build-kmp.sh
```

**What it does:**
- Cleans previous builds
- Builds Android variants for Core and UI
- Builds iOS frameworks for both modules

**Output:**
- `spectra-core/build/outputs/aar/spectra-core-release.aar`
- `spectra-ui/build/outputs/aar/spectra-ui-release.aar`

---

### `build-xcframework.sh`

Creates universal iOS XCFrameworks using the official KMP Gradle tasks.

```bash
./scripts/build/build-xcframework.sh [Release|Debug]
```

**What it does:**
1. Runs official `assembleXCFramework` tasks for both Core and UI
2. Centralizes the output into `build/xcframework/`

**Output:**
- `build/xcframework/SpectraLogger.xcframework`
- `build/xcframework/SpectraLoggerUI.xcframework`

**Used by:**
- iOS Native Example App (via root `Package.swift`)
- Distribution via Swift Package Manager

---

## Utility Scripts

### `sync-version.sh`

Syncs the version number from `gradle.properties` to all READMEs, documentation, and the `Package.swift` file.

```bash
./scripts/sync-version.sh [new_version]
```

### `clean-all.sh`

Removes all build artifacts, including Gradle caches, XCFrameworks, and derived data.

```bash
./scripts/setup/clean-all.sh
```

---

## Complete Documentation

See individual script files for detailed usage and options.

All scripts follow these conventions:
- Executable (`chmod +x`)
- Error handling (`set -e`)
- Clear output messages
- Return meaningful exit codes
