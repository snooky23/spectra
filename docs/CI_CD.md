# CI/CD Pipeline

This document describes the Continuous Integration and Continuous Deployment (CI/CD) setup for the Spectra Logger project.

## Overview

The project uses GitHub Actions for automated builds, testing, and code quality checks. The CI pipeline is optimized for speed through parallel execution, build caching, and Gradle Configuration Cache.

## Workflow Structure

```
┌──────────────────┐
│   code-quality   │  ← Runs first, fails fast (Ubuntu)
└────────┬─────────┘
         │
┌────────▼─────────┐
│    build-kmp     │  ← Builds KMP core and UI, caches output
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
| `build-kmp` | Ubuntu | code-quality | Build & test Core + UI modules |
| `build-android` | Ubuntu | build-kmp | Build Android Example |
| `build-ios` | macOS 15 | build-kmp | Build iOS XCFrameworks + Example |

**Optimizations:**
- **Gradle Configuration Cache:** Dramatically reduces build configuration time.
- **Concurrency:** Cancels in-progress runs for same branch/PR.
- **Caching:** KMP build output cached and reused by platform jobs.
- **Parallel execution:** Android and iOS build simultaneously.

### `security.yml` - Security Scans

**Triggers:** Push to `main`, Weekly schedule (Monday 00:00 UTC)

| Job | Purpose |
|-----|---------|
| `dependency-scan` | Check for vulnerable dependencies |
| `secret-scan` | Detect exposed secrets/credentials |
| `codeql` | Static security analysis (Kotlin) |
| `license-check` | Verify license compliance |

> Note: Security scans only run on `main` and weekly schedule, not on every PR.

### `release.yml` - Release Automation

**Triggers:** Git tags matching `v*`

Handles publishing to Maven Central and creating GitHub Releases with zipped XCFrameworks.

## Code Quality Configuration

### Modules Checked

Both **spectra-core** and **spectra-ui** are checked by ktlint and detekt.
Example apps are excluded because:
- Example apps are for demonstration, not production.
- Wildcard imports are common in Compose files.

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
./gradlew assemble

# Build specific modules
./gradlew :spectra-core:assemble
./gradlew :spectra-ui:assemble
```

### Tests
```bash
# Run all tests
./gradlew :spectra-core:allTests :spectra-ui:allTests
```

### iOS XCFrameworks
```bash
# Build both Core and UI XCFrameworks
./scripts/build/build-xcframework.sh
```

## Artifacts

The CI pipeline produces the following artifacts:

| Artifact | Description |
|----------|-------------|
| `kmp-test-results` | Test results from KMP Core and UI |
| `android-debug-apk` | Debug APK from Android example |
| `SpectraLogger-xcframeworks` | iOS XCFrameworks (Core + UI) |

## Troubleshooting

### Build Failures

1. **ktlint failures**: Run `./gradlew ktlintFormat` and commit fixes.
2. **Configuration Cache issues**: If you add new build logic, ensure it is serializable.
3. **iOS build failures**: Ensure Xcode command line tools are installed and `xcodebuild` is available.

### Cache Issues

```bash
# Clear Gradle cache
./gradlew clean

# Clear Gradle cache completely
rm -rf ~/.gradle/caches
```
