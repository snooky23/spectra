# Swift Package Manager Distribution Guide

This document explains how SpectraLogger is distributed via Swift Package Manager (SPM) using pre-built XCFramework binaries.

## Overview

SpectraLogger uses **binary distribution** via SPM, following industry best practices used by Firebase, Amplitude, and other major SDKs. This approach:

✅ Users don't need to build the Kotlin Multiplatform (KMP) framework themselves
✅ Faster dependency resolution and build times
✅ Proper semantic versioning
✅ Automatic checksum validation for security
✅ Works seamlessly with Xcode and Swift Package Manager

## Architecture

```
┌─────────────────────────────────────────────────┐
│         Kotlin Multiplatform Code               │
│         (shared/src/...)                        │
└────────────────┬────────────────────────────────┘
                 │
                 │ Gradle Build
                 ▼
┌─────────────────────────────────────────────────┐
│         iOS Frameworks                          │
│  ├─ iosArm64 (iPhone/iPad devices)             │
│  ├─ iosX64 (Intel simulator)                   │
│  └─ iosSimulatorArm64 (M1/M2 simulator)        │
└────────────────┬────────────────────────────────┘
                 │
                 │ xcodebuild -create-xcframework
                 ▼
┌─────────────────────────────────────────────────┐
│         XCFramework Bundle                      │
│         SpectraLogger.xcframework                │
└────────────────┬────────────────────────────────┘
                 │
                 │ Zip + Checksum
                 ▼
┌─────────────────────────────────────────────────┐
│         GitHub Release                          │
│  ├─ SpectraLogger.xcframework.zip               │
│  └─ SHA256 checksum                            │
└────────────────┬────────────────────────────────┘
                 │
                 │ Swift Package Manager
                 ▼
┌─────────────────────────────────────────────────┐
│         User's Xcode Project                    │
│         (Automatic download & integration)      │
└─────────────────────────────────────────────────┘
```

---

## For Users: Installing SpectraLogger

### Option 1: Xcode (Recommended)

1. **Open your Xcode project**
2. **File → Add Package Dependencies**
3. **Enter repository URL:**
   ```
   https://github.com/snooky23/Spectra.git
   ```
4. **Select version** (e.g., `1.0.0` or "Up to Next Major Version")
5. **Click "Add Package"**

Xcode will:
- Download the appropriate XCFramework
- Verify the checksum
- Link it to your target automatically

### Option 2: Package.swift

If you're building a Swift package, add this to your `Package.swift`:

```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "YourApp",
    platforms: [
        .iOS(.v13)
    ],
    dependencies: [
        .package(url: "https://github.com/snooky23/Spectra.git", from: "1.0.0")
    ],
    targets: [
        .target(
            name: "YourApp",
            dependencies: [
                .product(name: "SpectraLogger", package: "Spectra")
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

## For Maintainers: Creating a Release

### Prerequisites

- macOS with Xcode 14.0+
- Swift 5.9+
- Gradle 8.0+
- Git command line tools
- GitHub CLI (optional, for automated releases)

### Manual Release Process

#### 1. Prepare Release

```bash
# Ensure you're on main branch with latest changes
git checkout main
git pull origin main

# Update version in gradle.properties
# VERSION_NAME=1.0.0

# Commit version bump
git add gradle.properties
git commit -m "chore: bump version to 1.0.0"
git push origin main
```

#### 2. Create Release Tag

```bash
# Create and push tag
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

#### 3. GitHub Actions Will Automatically:

The `.github/workflows/release.yml` workflow will:

1. ✅ Build all iOS frameworks (Arm64, x86_64, SimulatorArm64)
2. ✅ Create XCFramework bundle
3. ✅ Zip the XCFramework
4. ✅ Calculate SHA256 checksum
5. ✅ Create GitHub Release with:
   - Release notes
   - XCFramework zip file
   - Installation instructions
   - Checksum for verification

#### 4. Update Package.swift

After the release is created, update `Package.swift`:

```swift
.binaryTarget(
    name: "SpectraLogger",
    url: "https://github.com/snooky23/Spectra/releases/download/v1.0.0/SpectraLogger.xcframework.zip",
    checksum: "abc123..." // Use checksum from release
)
```

Commit and push:
```bash
git add Package.swift
git commit -m "chore: update Package.swift for v1.0.0"
git push origin main
```

### Automated Release Process (Advanced)

Use the provided script:

```bash
# Build XCFramework and prepare release artifacts
./scripts/release/create-release.sh 1.0.0
```

This will:
1. Build the XCFramework
2. Create zip file
3. Calculate checksum
4. Generate Package.swift snippet
5. Create release summary

Then manually:
1. Create Git tag
2. Push tag (triggers GitHub Actions)
3. Update Package.swift with generated snippet

---

## Manual Build (For Development)

If you need to build the XCFramework locally:

### Build XCFramework

```bash
# Build release XCFramework
./scripts/build/build-xcframework.sh Release

# Or build debug XCFramework
./scripts/build/build-xcframework.sh Debug
```

Output location: `build/xcframework/SpectraLogger.xcframework`

### Use Local XCFramework

For local development, update `Package.swift` to use a local path:

```swift
.binaryTarget(
    name: "SpectraLogger",
    path: "build/xcframework/SpectraLogger.xcframework"
)
```

> **Note:** Don't commit Package.swift with local paths. This is for development only.

---

## Verification & Validation

### Verify XCFramework Contents

```bash
cd build/xcframework
ls -la SpectraLogger.xcframework

# Should contain:
# - Info.plist
# - ios-arm64/
# - ios-arm64_x86_64-simulator/
```

### Verify Checksum

```bash
# Calculate checksum of zip file
swift package compute-checksum SpectraLogger.xcframework.zip

# Should match the checksum in Package.swift
```

### Test in Sample Project

```bash
# Create test project
mkdir TestSpectra && cd TestSpectra

# Initialize SPM package
swift package init --type executable

# Add dependency to Package.swift
# ... add SpectraLogger dependency ...

# Resolve and build
swift package resolve
swift build
```

---

## Troubleshooting

### "Binary target checksum mismatch"

**Problem:** SPM reports checksum doesn't match.

**Solutions:**
1. Recalculate checksum:
   ```bash
   swift package compute-checksum path/to/SpectraLogger.xcframework.zip
   ```
2. Update Package.swift with correct checksum
3. Ensure you're downloading the correct version

### "Failed to download binary"

**Problem:** Cannot download XCFramework from GitHub.

**Solutions:**
1. Check internet connection
2. Verify GitHub release exists
3. Verify URL in Package.swift is correct
4. Check if release asset was uploaded successfully

### "No such module 'SpectraLogger'"

**Problem:** Xcode can't find the module.

**Solutions:**
1. Clean build folder: `Product → Clean Build Folder`
2. Reset package cache: `File → Packages → Reset Package Caches`
3. Resolve packages: `File → Packages → Resolve Package Versions`
4. Restart Xcode

### XCFramework Build Fails

**Problem:** `build-xcframework.sh` fails.

**Solutions:**
1. Ensure Xcode is installed: `xcode-select -p`
2. Select Xcode version: `sudo xcode-select -s /Applications/Xcode.app`
3. Install Xcode Command Line Tools: `xcode-select --install`
4. Check Java version: `java -version` (should be 17+)

---

## Best Practices

### Version Naming

Use **semantic versioning**:
- `1.0.0` - Major release (breaking changes)
- `1.1.0` - Minor release (new features, backward compatible)
- `1.0.1` - Patch release (bug fixes)

### Tagging Strategy

- Always use `v` prefix: `v1.0.0`
- Use annotated tags: `git tag -a v1.0.0 -m "Release 1.0.0"`
- Never delete released tags
- Use pre-release tags for betas: `v1.0.0-beta.1`

### Release Notes

Include in release notes:
- ✅ New features
- ✅ Bug fixes
- ✅ Breaking changes (if any)
- ✅ Installation instructions
- ✅ Checksum for verification
- ✅ Supported platforms

### Security

- ✅ Always include SHA256 checksum
- ✅ Use HTTPS URLs for downloads
- ✅ Sign releases with GPG (optional)
- ✅ Never commit secrets to repository

---

## Comparison: Before vs After

### Before (Local Path)

```swift
// ❌ Users must build KMP framework themselves
.binaryTarget(
    name: "SpectraLogger",
    path: "../shared/build/XCFrameworks/release/SpectraLogger.xcframework"
)
```

**Problems:**
- ❌ Users need Gradle, JDK, Kotlin toolchain
- ❌ Long build times (5-10 minutes)
- ❌ Platform-specific issues
- ❌ No versioning
- ❌ Breaks on version updates

### After (Remote URL)

```swift
// ✅ Automatic download, verified, versioned
.binaryTarget(
    name: "SpectraLogger",
    url: "https://github.com/snooky23/Spectra/releases/download/v1.0.0/SpectraLogger.xcframework.zip",
    checksum: "abc123..."
)
```

**Benefits:**
- ✅ Instant installation (download only)
- ✅ No build tools needed
- ✅ Semantic versioning
- ✅ Checksum validation
- ✅ Industry standard approach

---

## Reference

### Scripts

- `scripts/build/build-xcframework.sh` - Build XCFramework
- `scripts/release/create-release.sh` - Prepare release artifacts

### Workflows

- `.github/workflows/release.yml` - Automated release on tag push

### Files

- `Package.swift` - SPM manifest
- `build/xcframework/` - XCFramework output directory
- `build/releases/` - Release artifacts directory

---

## Support

For issues or questions:
- GitHub Issues: https://github.com/snooky23/Spectra/issues
- Discussions: https://github.com/snooky23/Spectra/discussions

---

**Document Version:** 1.0
**Last Updated:** 2025-10-08
**Maintained By:** Spectra Logger Team
