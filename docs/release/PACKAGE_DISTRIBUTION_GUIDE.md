# Package Distribution Guide

This document explains the current distribution options for Spectra Logger and the roadmap for full support.

## Current Status

| Package Manager | Status | Notes |
|---|---|---|
| **Swift Package Manager (SPM)** | ✅ Primary (Recommended) | Fully functional, local + release modes |
| **Maven Central** | 📋 Planned | For Kotlin/Android distribution |

---

## Swift Package Manager (Primary)

**Status**: ✅ **FULLY SUPPORTED**

### For End Users

Users can add Spectra Logger to their project in two ways:

#### Option 1: Local Development (if contributing)

```swift
// In Package.swift
dependencies: [
    .package(path: "../SpectraLoggerUI")
]
```

#### Option 2: Release Version (recommended for users)

```swift
// In Package.swift
dependencies: [
    .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0")
]
```

Or in Xcode:
1. File → Add Packages
2. Enter: `https://github.com/snooky23/spectra.git`
3. Select version
4. Add to your target

### Current Packages Available

- **SpectraLogger** - Core KMP framework wrapper
- **SpectraLoggerUI** - SwiftUI components

See [SWIFT_PACKAGE_SETUP.md](./SWIFT_PACKAGE_SETUP.md) for detailed setup.

---


**Status**: 🔧 **Under Development**

### Current State

Podspec files exist but need updates:

### Issues to Resolve

1. **Framework paths** - XCFramework location needs to be finalized
2. **Binary distribution** - Hosting pre-built frameworks
3. **License file** - Need LICENSE file in repositories
4. **Screenshot URLs** - Update documentation references

### Planned Support

Once completed, users will be able to:

```ruby
# In Podfile
```

This will automatically include:
- SpectraLogger (core framework)
- SpectraLoggerUI (UI components)

### Timeline

- [ ] Document in README

---

## Maven Central (Android/Kotlin)

**Status**: 📋 **Planned for 1.1.0**

For Android and KMP projects using Gradle:

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.spectra:logger:1.0.0")
}
```

---

## Recommendation by Use Case

### I want to use Spectra Logger in my iOS app

**Use Swift Package Manager** ✅
```swift
.package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0")
```

### I want to contribute to Spectra Logger

**Use Swift Package Manager (Local Mode)** ✅
```swift
.package(path: "../SpectraLoggerUI")
```


**Wait for 1.1.0 release** 🔧
- Planned support is coming
- Current implementation focuses on SPM

### I'm using Kotlin/Android

**Wait for Maven Central** 📋
- Planned for 1.1.0
- Currently use Gradle to build locally

---

## Publishing Roadmap

### Version 1.0.0 (Current - SPM Only)
- ✅ Swift Package Manager support
- ✅ Local development mode
- ✅ Release mode with version pinning
- ✅ Example app with SPM

### Version 1.1.0 (Planned)
- 📋 Maven Central for Android
- 📋 GitHub Releases for binaries

### Version 1.2.0+ (Future)
- 🎯 Swift Package Index listing
- 🎯 Carthage support (if requested)
- 🎯 SPM Binary Distribution

---

## Architecture

### Swift Package Manager (Current)

```
┌─────────────────────┐
│   Your iOS App      │
└────────┬────────────┘
         │ .package(url: "...")
┌────────▼─────────────────────────┐
│   GitHub Repository               │
│   ├── SpectraLogger/              │
│   │   ├── Package.swift           │
│   │   └── XCFrameworks/           │
│   └── SpectraLoggerUI/            │
│       ├── Package.swift           │
│       └── Sources/                │
└───────────────────────────────────┘
```


```
┌─────────────────────┐
│   Your iOS App      │
└────────┬────────────┘
┌────────▼─────────────────────────┐
└────────┬─────────────────────────┘
         │
┌────────▼─────────────────────────┐
│   GitHub Releases                 │
│   (Pre-built XCFrameworks)        │
└───────────────────────────────────┘
```

---

## Quick Decision Tree

```
Do you want to use Spectra Logger?
│
├─ Yes, in my iOS project
│  └─ Use Swift Package Manager ✅
│
├─ Yes, and I want to contribute
│  └─ Use SPM in Local Development Mode ✅
│
│  └─ Wait for v1.1.0 release 🔧
│
└─ Yes, and I'm using Android/Kotlin
   └─ Wait for Maven Central in v1.1.0 📋
```

---

## Contributing to Distribution


2. **Maven Central**: See issues tagged `maven`
3. **Binary Distribution**: See issues tagged `distribution`

See [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines.

---

## Support Matrix

|---------|-----|-----------|-------|----------|
| iOS Framework | ✅ | 🔧 | ❌ | ❌ |
| Android Library | ✅* | ❌ | 📋 | ❌ |
| KMP Projects | ✅ | 🔧 | 📋 | ❌ |
| Version Pinning | ✅ | ✅ | ✅ | ❌ |
| Binary Distribution | ✅ | 🔧 | 📋 | ❌ |

*Via local gradle build
✅ = Fully supported
🔧 = In progress
📋 = Planned
❌ = Not planned

---

## Questions?

See the relevant documentation:
- [Swift Package Setup](./SWIFT_PACKAGE_SETUP.md)
- [iOS Testing Guide](./iOS_TESTING_GUIDE.md)
- [README.md](./README.md)

---

**Last Updated**: October 30, 2025
**Version**: 1.0.0-SNAPSHOT
