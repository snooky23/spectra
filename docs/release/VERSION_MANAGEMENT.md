# Version Management Guide

## Overview

Spectra Logger consists of multiple packages that must be kept in sync:

1. **SpectraLogger (Kotlin Core)** - `shared/` module, published to Maven Central
2. **SpectraLoggerUI (Swift/SwiftUI)** - SwiftUI package, available via Swift Package Manager
3. **SpectraLogger (Swift Package)** - Binary wrapper for iOS integration

## Version Strategy

We use **Semantic Versioning** (MAJOR.MINOR.PATCH):
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

Current version: `0.0.1-SNAPSHOT` (defined in `gradle.properties`)

## How Versions Are Managed

### Primary Version Source: gradle.properties

The **single source of truth** is `gradle.properties`:

```properties
VERSION_NAME=0.0.1-SNAPSHOT
```

This version is used by:
- ✅ Kotlin `shared` module (Maven Central publication)
- ✅ All Gradle builds
- ✅ Version validation scripts

### Version Propagation

Swift packages (SpectraLogger and SpectraLoggerUI) use **git tags** for versioning:

```
git tag v0.0.1-SNAPSHOT
git push origin v0.0.1-SNAPSHOT
```

When users depend on these packages, they reference:

**SpectraLogger (Binary iOS Framework):**
```swift
.package(url: "https://github.com/snooky23/spectra.git", from: "0.0.1")
```

**SpectraLoggerUI (Native SwiftUI):**
```swift
.package(url: "https://github.com/snooky23/spectra.git", from: "0.0.1")
```

## Bumping Versions

### Step 1: Update gradle.properties

Edit `/gradle.properties` and change `VERSION_NAME`:

```bash
# Before
VERSION_NAME=0.0.1-SNAPSHOT

# After (example for minor version bump)
VERSION_NAME=0.0.2-SNAPSHOT
# or for pre-release
VERSION_NAME=0.0.2-beta1
# or for stable release
VERSION_NAME=0.0.2
```

### Step 2: Validate Version Alignment

Run the validation script:

```bash
./scripts/sync-versions.sh
```

This script will:
- ✅ Read VERSION_NAME from gradle.properties
- ✅ Validate all package files exist
- ✅ Display the current version across all packages

### Step 3: Build and Test

Run the full build to ensure everything compiles:

```bash
./gradlew build
```

### Step 4: Commit Changes

```bash
git add gradle.properties
git add shared/build.gradle.kts
git add SpectraLogger/build.gradle.kts
git add CHANGELOG.md (if updated)

git commit -m "release: bump version to 0.0.2"
```

### Step 5: Create Git Tag

```bash
# Create annotated tag (preferred)
git tag -a v0.0.2 -m "Release version 0.0.2"

# Or lightweight tag
git tag v0.0.2

# Push tags to GitHub
git push origin v0.0.2
```

### Step 6: Create GitHub Release

```bash
# Using gh CLI
gh release create v0.0.2 \
  --title "Version 0.0.2" \
  --notes "See CHANGELOG.md for details"
```

## Version in Different Contexts

### Kotlin Code (shared module)

Version is read from gradle.properties at build time:

```kotlin
// shared/build.gradle.kts
version = project.findProperty("VERSION_NAME") as String
```

When publishing to Maven Central:
```
com.spectra.logger:logger:0.0.2
```

### Swift Package (SpectraLogger)

Version is determined by git tag:

```swift
// Users depend like this:
.package(url: "https://github.com/snooky23/spectra.git", from: "0.0.2")
```

### Example Apps

Example apps are not versioned separately. They always use the latest version:

- `examples/ios-native/SpectraExample` - uses local SpectraLogger via relative path
- `examples/android-native/SpectraExample` - uses local SpectraLogger module

## Automation

### GitHub Actions Workflow

The CI/CD pipeline automatically:
1. ✅ Validates version in gradle.properties
2. ✅ Runs full build and tests
3. ✅ Validates code quality (ktlint, detekt)
4. ✅ (On release) Publishes to Maven Central
5. ✅ (On release) Creates iOS framework artifacts

### Manual Version Check

To verify all versions are aligned:

```bash
# Check gradle.properties
grep VERSION_NAME gradle.properties

# Validate Swift packages exist
ls SpectraLogger/Package.swift
ls SpectraLoggerUI/Package.swift

# Run validation script
./scripts/sync-versions.sh
```

## Version Alignment Checklist

Before each release, verify:

- [ ] VERSION_NAME updated in `gradle.properties`
- [ ] `./gradlew build` passes all tests
- [ ] Code quality checks pass: `./gradlew ktlintCheck detekt`
- [ ] CHANGELOG.md updated with changes
- [ ] All example apps compile and run
- [ ] iOS framework builds successfully
- [ ] Git tag created with format `v<version>`
- [ ] GitHub Release created with artifacts

## Files Involved in Version Management

```
Spectra/
├── gradle.properties              # PRIMARY version source
├── shared/build.gradle.kts        # Uses VERSION_NAME
├── SpectraLogger/
│   ├── build.gradle.kts           # Uses VERSION_NAME
│   └── Package.swift              # No explicit version (uses git tags)
├── SpectraLoggerUI/
│   └── Package.swift              # No explicit version (uses git tags)
├── CHANGELOG.md                   # Tracks version history
├── scripts/
│   └── sync-versions.sh           # Validation script
└── docs/
    └── VERSION_MANAGEMENT.md      # This file
```

## Important Notes

### Why Multiple Version Systems?

- **Kotlin (Maven Central)**: Uses artifact versioning
- **Swift (GitHub + SPM)**: Uses git tags and release tags

This is because:
- Maven Central requires explicit version metadata
- Swift Package Manager reads git tags as versions
- Git tags provide the source of truth for releases

### Snapshot Versions

While developing between releases, we use `-SNAPSHOT` suffix:

```
0.0.2-SNAPSHOT = pre-release, in development
0.0.2          = stable release
```

Users should pin to specific versions, not snapshots:

```swift
// ❌ Don't do this
.package(url: "...", from: "0.0.2-SNAPSHOT")

// ✅ Do this instead
.package(url: "...", from: "0.0.2")
```

### Pre-release Versions

For beta or release candidate versions:

```
0.0.2-alpha1    # First alpha
0.0.2-beta1     # First beta
0.0.2-rc1       # Release candidate
0.0.2           # Stable release
```

Users can opt into pre-releases:

```swift
.package(url: "...", revision: "v0.0.2-beta1")
```

## Quick Reference

```bash
# View current version
grep VERSION_NAME gradle.properties

# Bump to new version
sed -i '' 's/VERSION_NAME=.*/VERSION_NAME=0.0.2/' gradle.properties

# Validate versions are aligned
./scripts/sync-versions.sh

# Create release
./gradlew build && \
git tag -a v0.0.2 -m "Release v0.0.2" && \
git push origin v0.0.2
```

## Troubleshooting

### Q: Version mismatch between gradle and Swift packages?

A: Run `./scripts/sync-versions.sh` to validate. Versions should be:
- Kotlin: `gradle.properties`
- Swift: git tags (not in code)

### Q: How do users get specific version?

A: For Swift:
```swift
.package(url: "https://github.com/snooky23/spectra.git", from: "0.0.2")
```

For Kotlin:
```kotlin
dependencies {
    implementation("com.spectra.logger:logger:0.0.2")
}
```

### Q: Can I have different versions for UI and Core?

A: **No.** They must always be in sync:
- Same git tag
- Same package version
- Same release date

This ensures compatibility and reduces user confusion.

---

**Last Updated**: 2025-11-01
**Maintainer**: Claude Code
