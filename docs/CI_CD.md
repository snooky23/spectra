# CI/CD Pipeline

This document describes the Continuous Integration and Continuous Deployment (CI/CD) setup for the Spectra Logger project.

## Overview

The project uses GitHub Actions for automated builds, testing, and code quality checks. The CI pipeline is optimized for speed through parallel execution and build caching.

## Workflow Structure

```
┌──────────────────┐
│   code-quality   │  ← Runs first, fails fast (Ubuntu)
└────────┬─────────┘
         │
┌────────▼─────────┐
│    build-kmp     │  ← Builds KMP core, caches output
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌───▼───┐
│Android│ │ iOS   │  ← Parallel builds
└───────┘ └───────┘
```

## Workflows

### `ci.yml` - Main CI Pipeline

**Triggers:** Push to `main`/`develop`, Pull Requests

| Job | Runner | Dependencies | Purpose |
|-----|--------|--------------|---------|
| `code-quality` | Ubuntu | - | ktlint, detekt |
| `build-kmp` | Ubuntu | code-quality | Build & test core |
| `build-android` | Ubuntu | build-kmp | Build Android UI + Example |
| `build-ios` | macOS 15 | build-kmp | Build iOS XCFramework + Example |

**Optimizations:**
- **Concurrency:** Cancels in-progress runs for same branch/PR
- **Caching:** KMP build output cached and reused by platform jobs
- **Parallel execution:** Android and iOS build simultaneously

### `security.yml` - Security Scans

**Triggers:** Push to `main`, Weekly schedule (Monday 00:00 UTC)

| Job | Purpose |
|-----|---------|
| `dependency-scan` | Check for vulnerable dependencies |
| `secret-scan` | Detect exposed secrets/credentials |
| `codeql` | Static security analysis |
| `license-check` | Verify license compliance |

> Note: Security scans only run on `main` and weekly schedule, not on every PR.

### `release.yml` - Release Automation

**Triggers:** Git tags matching `v*`

Handles publishing to Maven Central and creating GitHub Releases.

## Code Quality Configuration

### Modules Checked

Only **spectra-core** is checked by ktlint and detekt. UI modules and examples are excluded because:
- Compose UI code uses many magic numbers for sizing/padding
- Example apps are for demonstration, not production
- Wildcard imports are common in Compose files

### Detekt Rules

Configuration: [`config/detekt/detekt.yml`](../config/detekt/detekt.yml)

| Rule | Status | Notes |
|------|--------|-------|
| `MagicNumber` | Disabled | Common in UI code |
| `WildcardImport` | Disabled | Common in Compose |
| `ForbiddenComment` | Disabled | TODOs are useful during dev |
| `MaxLineLength` | 140 chars | Increased for readability |

### ktlint Rules

Configuration: [`.editorconfig`](../.editorconfig)

```ini
[*.{kt,kts}]
max_line_length = 140
ktlint_standard_no-wildcard-imports = disabled
ktlint_standard_function-naming = disabled  # For Composable functions
```

## Running Locally

### Code Quality Checks
```bash
# Run ktlint and detekt
./gradlew ktlintCheck detekt

# Auto-fix formatting issues
./gradlew ktlintFormat
```

### Full Build
```bash
# Build everything
./gradlew build

# Build specific modules
./gradlew :spectra-core:build
./gradlew :spectra-ui-android:build
```

### Tests
```bash
# Run all tests
./gradlew allTests

# Run KMP tests only
./gradlew :spectra-core:allTests
```

## Artifacts

The CI pipeline produces the following artifacts:

| Artifact | Description |
|----------|-------------|
| `kmp-test-results` | Test results from KMP core |
| `android-debug-apk` | Debug APK from Android example |
| `SpectraLogger-xcframework` | iOS XCFramework for distribution |

## Troubleshooting

### Build Failures

1. **ktlint failures**: Run `./gradlew ktlintFormat` and commit fixes
2. **detekt issues**: Check the report at `build/reports/detekt/`
3. **iOS build failures**: Ensure Xcode command line tools are installed

### Cache Issues

```bash
# Clear Gradle cache
./gradlew clean

# Clear Gradle cache completely
rm -rf ~/.gradle/caches
```
