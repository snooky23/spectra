# Spectra Logger Scripts

This directory contains all build, test, and CI/CD scripts for the Spectra Logger project.

## Directory Structure

```
scripts/
├── build/          # Build scripts for all platforms
├── test/           # Test scripts
├── setup/          # Development environment setup
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

Builds the Kotlin Multiplatform shared library for all platforms.

```bash
./scripts/build/build-kmp.sh
```

**What it does:**
- Cleans previous builds
- Builds Android release variant
- Builds iOS frameworks (arm64, simulator-arm64, x64)

**Output:**
- `shared/build/bin/iosArm64/releaseFramework/SpectraLogger.framework`
- `shared/build/bin/iosSimulatorArm64/releaseFramework/SpectraLogger.framework`
- `shared/build/bin/iosX64/releaseFramework/SpectraLogger.framework`
- `shared/build/outputs/aar/shared-release.aar`

---

### `build-ios-xcframework.sh`

Creates a universal iOS XCFramework from the KMP frameworks.

```bash
./scripts/build/build-ios-xcframework.sh
```

**What it does:**
1. Builds KMP frameworks (calls `build-kmp.sh`)
2. Combines simulator architectures into FAT binary
3. Creates XCFramework with device + simulator support

**Output:**
- `shared/build/XCFrameworks/release/SpectraLogger.xcframework`

**Used by:**
- SpectraLoggerUI Swift Package (as binary dependency)
- Distribution via SPM/CocoaPods

---

## Complete Documentation

See individual script files for detailed usage and options.

All scripts follow these conventions:
- Executable (`chmod +x`)
- Error handling (`set -e`)
- Clear output messages
- Return meaningful exit codes
