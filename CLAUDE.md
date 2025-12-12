# Spectra Logger - AI Instructions

> Quick reference for AI assistants working on this Kotlin Multiplatform logging framework.

## Project Overview

**Spectra Logger** is a KMP logging framework for mobile apps (iOS + Android). It provides:
- Application event logging with severity levels
- Network request/response logging  
- On-device UI for viewing/filtering logs
- Export functionality

**Current Status**: v0.0.1-SNAPSHOT (under active development)

---

## Project Structure

```
Spectra/
├── shared/                      # Core KMP module (Kotlin)
│   └── src/
│       ├── commonMain/          # Shared business logic
│       ├── androidMain/         # Android-specific implementations
│       ├── iosMain/             # iOS-specific implementations
│       └── commonTest/          # Shared tests
│
├── SpectraLogger/               # iOS Swift Package (distribution)
│   ├── Package.swift            # SPM manifest
│   └── XCFrameworks/            # Pre-built frameworks
│
├── SpectraLoggerUI/             # iOS SwiftUI Package
│   ├── Package.swift
│   └── Sources/SpectraLoggerUI/
│       ├── Views/               # SwiftUI views
│       ├── ViewModels/          # MVVM view models
│       └── Components/          # Reusable UI components
│
├── examples/                    # Example apps
│   ├── android-native/          # Native Android demo
│   ├── ios-native/              # Native iOS demo
│   └── kmp-app/                 # KMP demo
│
├── scripts/                     # Build & CI scripts
│   ├── code-quality.sh          # ktlint + detekt
│   ├── build.sh, test.sh, ci.sh
│   └── bump-version.py          # Version management
│
└── docs/                        # Documentation
    ├── PLANNING.md              # Architecture decisions
    ├── TASKS.md                 # Development timeline
    ├── SESSION.md               # Current state/progress
    ├── ARCHITECTURE.md          # Technical architecture
    ├── API.md                   # API reference
    └── ...                      # Other guides
```

---

## Key Files to Read

When starting work, check these files in `docs/`:

| File | Purpose |
|------|---------|
| `docs/SESSION.md` | Current progress, what was done last, blockers |
| `docs/TASKS.md` | Task breakdown and milestones |
| `docs/PLANNING.md` | Architecture decisions and design |

---

## Code Quality

**Always run before committing:**
```bash
./scripts/code-quality.sh
```

Auto-fix formatting:
```bash
./gradlew ktlintFormat
```

---

## Build & Test

```bash
# Build everything
./gradlew build

# Run tests
./gradlew test

# Full CI pipeline
./scripts/ci.sh

# iOS Swift package tests
cd SpectraLoggerUI && swift test
```

---

## Architecture Notes

### KMP Code (shared/)
- **Clean Architecture**: Domain → Data → Platform layers
- **MVVM** for UI state management
- **expect/actual** for platform-specific code
- Domain layer has no platform dependencies

### Swift Packages (iOS)
- `SpectraLogger/` - Distribution package with XCFrameworks
- `SpectraLoggerUI/` - SwiftUI components for log viewer

### Key Patterns
- Circular buffers for bounded memory
- Thread-safe logging (atomic operations)
- Flow/StateFlow for reactive updates

---

## Commit Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(core): add log rotation support
fix(android): resolve threading issue in interceptor  
docs(readme): update installation instructions
test(domain): add FilterLogsUseCase tests
```

---

## Performance Targets

| Operation | Target | Critical |
|-----------|--------|----------|
| Log capture | < 0.1ms | < 1ms |
| Network intercept | < 5ms | < 20ms |
| UI scroll | 60 FPS | - |
| Memory (10K logs) | < 50MB | - |

---

## Quick Reference

### Logging API
```kotlin
SpectraLogger.d("TAG", "Debug message")
SpectraLogger.e("TAG", "Error", throwable = exception)
SpectraLogger.i("TAG", "Info", metadata = mapOf("key" to "value"))
```

### Show Logger UI
```kotlin
SpectraLogger.showScreen()
SpectraLogger.dismissScreen()
```

---

**Last Updated**: 2025-12-12
