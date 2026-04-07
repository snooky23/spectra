# Architectural Specification: Spectra Logger KMP UI SDK & Adaptive Layouts

**Status:** Approved  
**Author:** Spectra Architecture Team  
**Date:** April 2026  

---

## 1. Executive Summary
The Spectra Logger project currently maintains divergent UI implementations for Android (`spectra-ui-android`) and iOS (`spectra-ui-ios`). This specification outlines the strategic migration to a unified **Compose Multiplatform (CMP)** architecture within a new `spectra-ui` Kotlin Multiplatform (KMP) module. 

Furthermore, this migration serves as the vehicle to implement mandatory **Android 17 Large Screen Guidelines** (handling devices with `sw >= 600dp` natively without letterboxing). The resulting SDK will provide a "write once, run natively" debug interface that seamlessly integrates into both modern Android Compose and iOS SwiftUI host applications.

---

## 2. Target Architecture

### 2.1 Module Topology
We are transitioning from a platform-siloed approach to a strictly layered KMP architecture:

*   **`spectra-core` (Unchanged)**: Contains platform-agnostic business logic, storage (SQLite/InMemory), network interceptors, and data models.
*   **`spectra-ui` (NEW)**: The unified UI SDK.
    *   **`commonMain`**: 100% of the UI rendering, state management (ViewModels), navigation, and adaptive scaffolding.
    *   **`androidMain`**: Android-specific lifecycle hooks and window integrations (e.g., `SpectraLoggerFabOverlay`, `Intent` sharing).
    *   **`iosMain`**: iOS-specific bridging. Exports the CMP UI as a `UIViewController` via `ComposeUIViewController`. Manages `UIActivityViewController` for sharing.
*   **`spectra-ui-android` & `spectra-ui-ios`**: Deprecated and scheduled for deletion to eliminate technical debt.

### 2.2 Host Application Integration
The example apps act as the canonical consumers of the SDK, proving its interoperability.
*   **Android Host (`examples/android-native`)**: Continues as a pure Jetpack Compose application. It will consume `spectra-ui` Composables directly.
*   **iOS Host (`examples/ios-native`)**: Continues as a pure SwiftUI application. It will consume the exported `SpectraLogger` XCFramework. A SwiftUI `UIViewControllerRepresentable` will act as the host boundary for the KMP UI.

---

## 3. Android 17 Compliance & Adaptive UI Strategy

With Android 17 mandating edge-to-edge adaptive layouts on devices `sw >= 600dp`, the `spectra-ui` module will leverage the **Material 3 Adaptive Library**.

### 3.1 Adaptive Navigation (`NavigationSuiteScaffold`)
Top-level navigation must respond dynamically to `WindowSizeClass`:
*   **Compact (Phones)**: Bottom Navigation Bar.
*   **Medium (Foldables/Small Tablets)**: Navigation Rail (left-aligned).
*   **Expanded (Large Tablets/Desktop Mode)**: Persistent Navigation Drawer.

### 3.2 Canonical Layouts (`NavigableListDetailPaneScaffold`)
The Log and Network screens will transition from stacked screens to pane-based architectures:
*   **Single-Pane (Compact)**: List view only. Navigation pushes the Detail view onto the stack.
*   **Dual-Pane (Medium/Expanded)**: List remains persistent on the leading edge; Detail view renders simultaneously on the trailing edge.
*   **State Preservation**: The `rememberListDetailPaneScaffoldNavigator<T>()` will track the active pane and selected item. To survive Android configuration changes (rotation, freeform window resizing) and iOS state restoration, the navigator's state will be backed by a KMP-compatible `Parcelable` implementation (`@CommonParcelize`).

### 3.3 Predictive Back Gestures
The use of `NavigableListDetailPaneScaffold` natively integrates with Android 17's predictive back system. On iOS, the Compose Multiplatform runtime will translate standard swipe-to-go-back gestures to the CMP navigator.

---

## 4. Swift Interoperability & SKIE Integration

Exposing Kotlin code to Swift via Objective-C headers historically degrades the developer experience. To maintain a world-class API for iOS consumers, we will integrate **SKIE (Swift-Kotlin Interface Enhancer)** into the build pipeline.

### 4.1 SKIE Capabilities Utilized
1.  **Coroutines & Flows**: SKIE will transform Kotlin `Flow<T>` exposed by our SDK into native Swift `AsyncSequence`, allowing iOS developers to use idiomatic `for await` loops.
2.  **Suspend Functions**: Kotlin `suspend` functions will bridge directly to Swift `async throws`, bypassing legacy completion handlers.
3.  **Exhaustive Enums**: Kotlin Sealed Classes (e.g., UI States or Log Levels) will bridge as exhaustive Swift `enum` types, enabling safe `switch` statements without default clauses.

---

## 5. CI/CD, Distribution & Deprecations


This migration changes the distribution model and deprecates legacy tools:
1. **XCFramework Generation**: The GitHub Actions workflows must be updated to build and package the new `spectra-ui` KMP module into a binary `SpectraLoggerUI.xcframework` alongside the core framework.
2. **Swift Package Manager (`Package.swift`)**: The package manifest will be updated to distribute `SpectraLoggerUI` as a `binaryTarget` (downloading the pre-compiled CMP XCFramework) instead of compiling raw Swift files. This is the **sole** supported distribution method for iOS moving forward.
4. **Android Publishing**: The `mavenPublishing` setup will automatically include the new `spectra-ui` module when publishing to Maven Central, ensuring Android developers simply declare `implementation("com.spectra.logger:spectra-ui:VERSION")`.

---

## 6. Testing & Validation Strategy

A critical part of this migration is ensuring UI reliability and preventing regressions.

### 6.1 Unit & UI Testing
- **`commonTest`**: The Compose Multiplatform UI layer will be tested using Compose UI testing frameworks within the shared module. ViewModels will be unit-tested extensively.
- **Screenshot Testing (Optional/Future)**: Consider integrating Paparazzi or Roborazzi for cross-platform visual regression testing of the adaptive layouts.

### 6.2 Host App Verification
- The `examples/android-native` and `examples/ios-native` apps will serve as the primary integration tests. 
- CI must verify that both example apps compile successfully against the newly generated SDK artifacts before a release can be published.

---

## 7. Execution Plan

The migration will be executed in discrete, verifiable phases:

### Phase 1: KMP UI Module Foundation
- Create the `spectra-ui` module and configure the KMP Gradle script for CMP.
- Add dependencies: `compose.ui`, `compose.material3`, `adaptive`, `adaptive-layout`, `adaptive-navigation-suite`.
- Apply the `co.touchlab.skie` plugin to enhance the iOS target outputs.
- Implement the `expect/actual` bridge for `@CommonParcelize`.

### Phase 2: UI Migration & Refactoring
- Port existing Android Compose screens (Logs, Network, Settings) into `spectra-ui/commonMain`.
- Implement `NavigationSuiteScaffold` at the root of `SpectraLoggerScreen`.
- Implement `NavigableListDetailPaneScaffold` for list-detail interactions.
- Refactor sharing mechanisms to use `expect/actual` functions (Android `Intent` vs iOS `UIActivityViewController`).

### Phase 3: Platform Integration & Bridging
- **Android**: Wire up `SpectraUIManager` and the draggable `SpectraLoggerFabOverlay`.
- **iOS**: Create `SpectraViewController.kt` using `ComposeUIViewController`. Ensure safe insets and safe area boundaries are respected.

### Phase 4: Host App Updates & Cleanup
- Update `examples/android-native` to consume the new SDK.
- Update `examples/ios-native` with the SwiftUI wrapper for the CMP controller.
- Delete `spectra-ui-android` and `spectra-ui-ios` modules.

### Phase 5: Documentation & Release Preparation
- Ensure all usage guides provide clear examples of wrapping the new `SpectraViewController` for iOS consumers.

---

## 8. Version Control & Commit Standards
To maintain a pristine and understandable project history, all changes executed during this migration must strictly adhere to the **Google/Conventional Commits** standard (e.g., `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`). 
Furthermore, commits must be granular and atomic—every discrete logical change (e.g., "create module", "migrate logs screen", "update docs") must be committed independently before moving to the next.

---

## 9. Success Metrics
1.  **Code Reduction**: Significant reduction in total UI lines of code by eliminating the redundant native iOS implementation.
2.  **Adaptive Resilience**: The Android app flawlessly handles aggressive resizing in Android 17 Desktop Mode and fold state changes without losing the selected log detail state.
3.  **Native Feel**: The iOS app scrolls at 120hz, respects safe areas, and supports swipe-to-back gestures.
4.  **API Quality**: The generated Swift API is indistinguishable from a natively written Swift framework, verified via SKIE integration.
