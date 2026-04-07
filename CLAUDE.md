# Spectra Logger - AI Instructions

> Quick reference for AI assistants working on this Kotlin Multiplatform logging framework.

## Project Overview

**Spectra Logger** is a KMP logging framework for mobile apps (iOS + Android). It provides:
- Application event logging with severity levels
- Network request/response logging  
- Unified on-device UI (Compose Multiplatform) for viewing/filtering logs
- Export functionality

**Current Status**: v1.0.4

---

## Project Structure

```
Spectra/
├── spectra-core/                # Core KMP module (Kotlin) - Logic & Storage
│   └── src/
│       ├── commonMain/          # Shared business logic
│       ├── androidMain/         # Android-specific implementations
│       ├── iosMain/             # iOS-specific implementations
│       └── commonTest/          # Shared tests
│
├── spectra-ui/                  # Unified UI module (Compose Multiplatform)
│   └── src/
│       ├── commonMain/          # Shared UI (Adaptive Navigation, Detail Panes)
│       ├── androidMain/         # Android integration (FAB, Overlay)
│       └── iosMain/             # iOS integration (SwiftUI Bridge)
│
├── examples/                    # Example apps
│   ├── android-native/          # Native Android demo using spectra-ui
│   ├── ios-native/              # Native iOS demo using SpectraLoggerUI.xcframework
│   └── kmp-app/                 # KMP demo
│
├── scripts/                     # Build & CI scripts
│   ├── build/                   # Modular build scripts (KMP, XCFramework, etc)
│   ├── code-quality.sh          # ktlint + detekt
│   ├── ci.sh, test.sh
│   └── bump-version.py          # Version management
│
└── docs/                        # Documentation
    ├── guides/                  # User guides
    ├── design/                  # Design specs
    ├── release/                 # Release notes
    └── internal/                # Internal docs
```

---

## Key Files to Read

When starting work, check these files in `docs/`:

| File | Purpose |
|------|---------|
| `docs/internal/SESSION.md` | Current progress, what was done last, blockers |
| `TASKS.md` | Project tasks and milestones tracking (Root) |
| `docs/internal/PLANNING.md` | Architecture decisions and design |
| `docs/design/KMP_UI_ADAPTIVE_SPEC.md` | Unified UI Architecture specification |

---

## Code Quality

**Always run before committing:**
```bash
./gradlew ktlintCheck detekt
```

Auto-fix formatting:
```bash
./gradlew ktlintFormat
```

---

## Build & Test

```bash
# Build KMP modules
./gradlew :spectra-core:assemble :spectra-ui:assemble

# Build iOS XCFrameworks (Device + Simulator)
./scripts/build/build-xcframework.sh

# Run all tests
./gradlew :spectra-core:allTests :spectra-ui:allTests

# Full CI pipeline simulation
./scripts/ci.sh
```

---

## Architecture Notes

### Unified UI (spectra-ui/)
- **Compose Multiplatform**: Single UI implementation for both platforms.
- **Adaptive UI**: Uses `NavigationSuiteScaffold` and `NavigableListDetailPaneScaffold` for large-screen support.
- **Platform Bridging**:
  - Android: `SpectraLoggerFabOverlay` for easy integration.
  - iOS: `ComposeUIViewController` wrapped for SwiftUI via SKIE.

### Architecture
- **Clean Architecture**: Domain → Data → Platform layers
- **MVVM** for UI state management (Jetbrains Lifecycle)
- **expect/actual** for platform-specific code
- Circular buffers for bounded memory
- Thread-safe logging (atomic operations)

---

## Commit Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(ui): add search filtering to network logs
fix(android): resolve memory leak in FAB overlay
perf: enable Gradle configuration cache
docs: update integration guide for v1.0.4
```

---

## Quick Reference

### Logging API
```kotlin
SpectraLogger.d("TAG", "Debug message")
SpectraLogger.e("TAG", "Error", throwable = exception)
SpectraLogger.i("TAG", "Info", metadata = mapOf("key" to "value"))
```

### Show Logger UI (Kotlin/Android)
```kotlin
SpectraLogger.showScreen() // Or use SpectraLoggerFabOverlay
```

### Show Logger UI (Swift/iOS)
```swift
import SpectraUI
// ...
.sheet(isPresented: $showLogs) {
    SpectraLoggerView()
}
```

---

**Last Updated**: 2026-04-07
