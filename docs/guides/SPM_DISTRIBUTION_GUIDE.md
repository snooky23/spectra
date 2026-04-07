# Swift Package Manager Distribution Guide

This document explains how SpectraLogger is distributed via Swift Package Manager (SPM) using pre-built XCFramework binaries for both Core and UI components.

## Overview

SpectraLogger uses **binary distribution** via SPM, following industry best practices. This approach:

✅ Users don't need to build the Kotlin Multiplatform (KMP) frameworks themselves.
✅ Faster dependency resolution and build times.
✅ Proper semantic versioning for both Core and UI modules.
✅ Automatic checksum validation for security.
✅ Works seamlessly with Xcode and Swift Package Manager.

## Architecture

```
┌─────────────────────────────────────────────────┐
│         Kotlin Multiplatform Code               │
│      (spectra-core/src, spectra-ui/src)         │
└────────────────┬────────────────────────────────┘
                 │
                 │ Gradle Build (KMP Plugin)
                 ▼
┌─────────────────────────────────────────────────┐
│         iOS XCFramework Bundles                 │
│  ├─ SpectraLogger.xcframework (Core)            │
│  └─ SpectraLoggerUI.xcframework (Unified UI)    │
└────────────────┬────────────────────────────────┘
                 │
                 │ Zip + Checksum
                 ▼
┌─────────────────────────────────────────────────┐
│         GitHub Release                          │
│  ├─ SpectraLogger.xcframework.zip               │
│  ├─ SpectraLoggerUI.xcframework.zip             │
│  └─ SHA256 checksums                            │
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
3. **Enter repository URL:** `https://github.com/snooky23/spectra.git`
4. **Select version** (e.g., `1.0.4`)
5. **Click "Add Package"**
6. **Select products:**
   - `SpectraLogger` (Required - Core Logic)
   - `SpectraLoggerUI` (Optional - Unified UI SDK)

### Option 2: Package.swift

Add this to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.4")
],
targets: [
    .target(
        name: "YourApp",
        dependencies: [
            .product(name: "SpectraLogger", package: "Spectra"),
            .product(name: "SpectraLoggerUI", package: "Spectra")
        ]
    )
]
```

---

## For Maintainers: Creating a Release

### Automated Release Process

1. **Update Version:** Bump `VERSION_NAME` in `gradle.properties`.
2. **Sync Versions:** Run `./scripts/sync-versions.sh` to update `Package.swift` URLs.
3. **Commit & Tag:**
   ```bash
   git add .
   git commit -m "chore: release v1.0.4"
   git tag -a v1.0.4 -m "Release 1.0.4"
   git push origin main --tags
   ```
4. **GitHub Actions:** The `release.yml` workflow will automatically:
   - Build XCFrameworks for both modules using official KMP tasks.
   - Zip and calculate checksums.
   - Create a GitHub Release and attach the binaries.

---

## Manual Build (For Development)

If you need to build the XCFrameworks locally for testing:

```bash
# Build both release XCFrameworks
./scripts/build/build-xcframework.sh Release
```

Output location: `build/xcframework/`

---

## Verification & Validation

### Verify XCFramework Contents

```bash
cd build/xcframework
ls -la SpectraLoggerUI.xcframework

# Should contain:
# - Info.plist
# - ios-arm64/
# - ios-arm64_x86_64-simulator/
```

### Verify Checksum

```bash
swift package compute-checksum SpectraLoggerUI.xcframework.zip
```

---

## Troubleshooting

### "Binary target checksum mismatch"
Recalculate checksums for both files and ensure they match the values in the root `Package.swift`.

### "No such module 'SpectraLoggerUI'"
Ensure you have linked the library in your target's **Frameworks, Libraries, and Embedded Content** section.

---

**Document Version:** 1.1
**Last Updated:** 2026-04-07
**Maintainer:** Spectra Team
