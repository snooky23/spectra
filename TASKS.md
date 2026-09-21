# Spectra Logger: Project Tasks & PRD Tracking

This document tracks the implementation progress of Spectra Logger against the formal Product Requirements Document (`docs/design/PRD.md`).

## Phase 1: Foundation (Weeks 1-4)

- [x] **Milestone 1.1: Project Setup**
  - Repository initialization, KMP module structure, Gradle build, CI/CD, Documentation
- [x] **Milestone 1.2: Core Logging**
  - LogManager, Logger instances, levels, filtering, context, unit tests
- [x] **Milestone 1.3: Platform Storage**
  - File-based storage (Android/iOS), export APIs, performance benchmarks

## Phase 2: Network Logging (Weeks 5-7)

- [x] **Milestone 2.1: Android Network Interception**
  - OkHttp interceptor, capture details, filtering
- [x] **Milestone 2.2: iOS Network Interception**
  - URLProtocol subclass, capture details, filtering
- [x] **Milestone 2.3: Network Log Storage**
  - Dedicated storage, Network-specific models, search capabilities

## Phase 3: UI Development (Weeks 8-12)

- [x] **Milestone 3.1: Log Viewer Screen**
  - List view, log level filtering, search, platform UI integration
- [x] **Milestone 3.2: Detail Views**
  - Log detail view, network detail view, context formatting, copy/share actions
- [x] **Milestone 3.3: Network Viewer Screen**
  - Network request list, method/status filtering, request/response tabs
- [x] **Milestone 3.4: Settings & Polish**
  - Settings screen (Configuration UI), access mechanisms (FAB, gesture), dark mode support, accessibility

## Phase 4: Integration & Testing (Weeks 13-15)

- [x] **Milestone 4.1: Example Apps**
  - Sample Android app, sample iOS app, sample KMP app
- [x] **Milestone 4.2: Performance Testing**
  - Load testing (10K+ logs), memory profiling, network overhead measurement
- [x] **Milestone 4.3: Documentation**
  - Completed API documentation, Integration guides, Configuration reference, Architecture documentation, and Troubleshooting guide

## Phase 5: Beta & Launch (Weeks 16-18)

- [x] **Milestone 5.1: Beta Release**
  - Initial releases and snapshot builds
- [ ] **Milestone 5.2: Bug Fixes & Refinement**
  - Address feedback, fix high/critical bugs, perf improvements
- [x] **Milestone 5.3: 1.0 Launch**
  - Published artifacts (Maven Central, Cocoapods/SPM), GitHub release automation

## Phase 6: Post-Launch (Ongoing)

- [x] Remote log streaming (WebSocket)
- [ ] Log analytics dashboard
- [ ] Crash symbolication
- [x] Plugin architecture for custom sinks
- [ ] Integration with crash reporting services (Sentry, Firebase)

## Phase 7: KMP UI SDK Migration & Android 17 Compliance

See `docs/design/KMP_UI_ADAPTIVE_SPEC.md` for full architectural details.

- [x] **Phase 1: KMP UI Module Foundation**
  - Create the `spectra-ui` module, configure CMP dependencies, and implement `@CommonParcelize`.
- [x] **Phase 2: UI Migration & Refactoring**
  - Port existing Compose screens, implement `NavigationSuiteScaffold` and `NavigableListDetailPaneScaffold`.
- [x] **Phase 3: Platform Integration & Bridging**
  - Implement `SpectraLoggerFabOverlay` (Android) and `ComposeUIViewController` (iOS) with SKIE enhancements.
- [x] **Phase 4: Host App Updates & Cleanup**
  - Update example apps to use the unified SDK and delete the deprecated native UI modules (`spectra-ui-android`, `spectra-ui-ios`).
- [x] **Phase 5: Documentation & Release Preparation**
  - Update READMEs, CI/CD GitHub Actions, and `Package.swift` for binary distribution. Drop CocoaPods support.
- [x] **Phase 6: Bug Fixes & Unit Testing**
  - [x] Fix stack trace extraction from metadata in `SpectraLogger`.
  - [x] Add explicit HTTP error simulation to example applications.
  - [x] Implement comprehensive unit tests for error/metadata logging and network filtering.
  - [x] Target near 100% test coverage for core and UI logic.

## Phase 8: Build & Performance Optimization

- [x] **Phase 8.1: Build Speed Improvements**
  - [x] Enable and optimize Gradle configuration cache for faster incremental builds.
  - [x] Investigate and resolve any configuration cache incompatibilities in the build scripts.

## Phase 9: Advanced Build & Performance Optimization

- [x] **Phase 9.1: Build Analytics & Bottleneck Identification**
  - [x] Integrate Gradle Build Scans (Develocity) for deep performance analysis.
  - [x] Resolve configuration-time resolution issues for better build scalability.
- [x] **Phase 9.2: Compilation & Tooling Optimization**
  - [x] Fine-tune Kotlin Daemon and JVM memory settings for better compilation throughput.
  - [x] Implement modern Kotlin Multiplatform DSL and latest Gradle features.
  - [x] Suppress machine-specific native target warnings.
  - [x] Fix ktlint violations in build scripts and source code.
- [x] **Phase 9.3: CI/CD Pipeline Acceleration**
  - [x] Implement remote build caching for GitHub Actions to share build artifacts across PRs.
  - [x] Optimize XCFramework generation to only build required architectures for testing.

## Phase 10: Universal Platform Support & Okio Migration

- [x] **Phase 10.1: Disk I/O Abstraction**
  - [x] Migrate `spectra-core` disk writing to use `com.squareup.okio:okio`.
  - [x] Remove legacy `expect`/`actual` logic for FileSystem on Android and iOS.
- [x] **Phase 10.2: Cross-Platform Dependencies**
  - [x] Replace `expect`/`actual` for `IdGenerator` with `kotlin.uuid.Uuid`.
  - [x] Replace `expect`/`actual` for `SourceDetector` with `Exception().stackTraceToString()`.
- [x] **Phase 10.3: Desktop and Web Targets**
  - [x] Expand `build.gradle.kts` to support `jvm`, `macosX64`, `macosArm64`, `linuxX64`, `linuxArm64`, `mingwX64`.
  - [x] Expand `build.gradle.kts` to support Web targets (`js`, `wasmJs`) with memory-only fallbacks for I/O.
  - [x] Verify multiplatform coroutines test execution (`runTest`) across all targets.

## Phase 11: Repository Hygiene & Codebase Cleanup

See `_bmad-output/planning-artifacts/spec-cleanup/SPEC.md` and `SDD.md` for architectural design and capability specifications.

- [x] **Phase 11.1: Root Scratch & Cruft Deletion**
  - [x] Delete transitional migration scripts (`fix_imports_2.py`, `fix_imports_3.py`, `refactor.py`, `add_build_phase.rb`).
  - [x] Remove scratch test file (`test.kt`) and local build logs (`build-xcframework.log`).
- [x] **Phase 11.2: Git & IDE Configuration Hygiene**
  - [x] Update root `.gitignore` to recursively ignore nested `.idea` caches across all subdirectories.
  - [x] Untrack `examples/.idea/caches/deviceStreaming.xml` from Git.
  - [x] Clean up `.idea/compiler.xml` bytecode target levels to match active modules.
- [x] **Phase 11.3: Build & Settings Harmonization**
  - [x] Annotate commented-out sample inclusions in `settings.gradle.kts`.
- [x] **Phase 11.4: Validation & Quality Gate**
  - [x] Verify clean git status and run validation checks across multiplatform targets.

## Phase 12: Events Tab (Screen Views & User Analytics)

See `_bmad-output/planning-artifacts/spec-events-webview-streaming/SPEC.md` and `SDD.md` for architectural design and capability specifications.

- [x] **Phase 12.1: Core Event Data Models & Storage**
  - [x] Implement `EventLogEntry`, `EventType`, and `EventFilter` in `spectra-core/src/commonMain`.
  - [x] Implement `EventLogStorage` and `InMemoryEventLogStorage` with coroutine flow support.
  - [x] Write comprehensive unit tests for event storage, filtering, and flows.
- [x] **Phase 12.2: SpectraLogger Public Telemetry API**
  - [x] Add `SpectraLogger.event(name, parameters)`.
  - [x] Add `SpectraLogger.screenStart(screenName)` and `SpectraLogger.screenEnd(screenName)` with automatic duration calculation.
  - [x] Ensure non-blocking background coroutine dispatch and thread safety.
- [x] **Phase 12.3: Compose Multiplatform Events UI & Timeline**
  - [x] Implement `EventsScreen` with `AdaptiveNavigator` (Dual-pane adaptive layout).
  - [x] Add Events tab to `SpectraNavigationSuiteScaffold` between Network and Settings.
  - [x] Build interactive event timeline, duration badges, and parameter inspector.
- [x] **Phase 12.4: Clean Code Architecture, Low Coupling & High Cohesion Hardening**
  - [x] Decouple `EventsViewModel` from static `SpectraLogger` singleton via constructor injection (`eventStorage: EventLogStorage`).
  - [x] Decouple ViewModel clear action to operate strictly through the injected storage interface.
  - [x] Add hermetic unit test in `EventsViewModelTest` using isolated in-memory storage.
  - [x] Run multiplatform validation and verify zero cross-module leakage between core and UI.

## Phase 13: WebView & JavaScript Logging

- [x] **Phase 13.1: Android WebView Console Interception**
  - [x] Implement `SpectraWebChromeClient` to capture `console.log/warn/error` and unhandled exceptions.
  - [x] Add `WebView.attachSpectraLogger()` extension in `androidMain`.
- [x] **Phase 13.2: iOS WKWebView Console Interception**
  - [x] Implement `SpectraScriptMessageHandler` and JS console proxy script in `iosMain`.
  - [x] Add `WKWebView.attachSpectraLogger()` bridging helper.
- [x] **Phase 13.3: WebView Log Tagging & UI Integration**
  - [x] Add `[WebView]` tag chips and dedicated filter controls in `spectra-ui`.

## Phase 14: Remote Log Streaming (WebSocket)

- [x] **Phase 14.1: Streaming Protocol Models & Serialization**
  - [x] Define `StreamPacket` hierarchy (`HandshakeRequest`, `HandshakeResponse`, `BatchHistory`, `LiveLog`, `LiveNetwork`, `LiveEvent`, `Ping`, `Pong`).
- [x] **Phase 14.2: Mobile WebSocket Client & Wi-Fi Pairing Engine**
  - [x] Implement Ktor WebSocket client in `spectra-core` (`KtorStreamTransport`, `DefaultSpectraStreamClient`).
  - [x] Implement two-phase sync: historical catch-up batch followed by live incremental stream.
- [x] **Phase 14.3: Mobile QR Code Scanner Integration**
  - [x] Implement pairing dialog and QR code decoder/parser in `spectra-ui`.
  - [x] Add connection status banner and pairing flow (`RemoteStreamBanner`, `RemoteStreamDialog`, `RemoteStreamViewModel`).
- [x] **Phase 14.4: Desktop Browser Companion & Authorization Gatekeeper**
  - [x] Create desktop browser companion dashboard displaying pairing QR code on local Wi-Fi (`tools/desktop-companion`).
  - [x] Implement browser authorization prompt ("Allow [Device] to stream?").
  - [x] Render mirrored live telemetry using the big-screen adaptive dual-pane UI.
- [x] **Phase 14.5: Clean Code Architecture & Low Coupling Hardening**
  - [x] Decouple iOS `SpectraWKScriptMessageHandler` via constructor-injected log sink function to remove global singleton coupling.
  - [x] Decouple `DefaultSpectraStreamClient` by eliminating circular imports and dependencies on the `SpectraLogger` facade.
  - [x] Verify complete dependency inversion and interface-driven design across all multiplatform streaming and webview components.

## Phase 15: Enhanced Multi-Format Exporters, HAR 1.2 & Telemetry Bundle

- [x] **Phase 15.1: Events Multi-Format Export Support**
  - [x] Implement `exportEventsAsText`, `exportEventsAsJson`, `exportEventsAsCsv`, and `exportEventsAsMarkdown` in `LogExporter`.
- [x] **Phase 15.2: Network HAR 1.2 Format Exporter**
  - [x] Implement standard HTTP Archive 1.2 serialization (`HarRoot`, `HarLog`, `HarEntry`, `HarRequest`, `HarResponse`) for Chrome DevTools / Charles / Proxyman.
- [x] **Phase 15.3: Full Unified Telemetry Debug Bundle**
  - [x] Implement comprehensive debug bundle export combining Logs, Network, Events, and Device Context.
- [x] **Phase 15.4: SettingsViewModel Decoupling & UI Integration**
  - [x] Decouple `SettingsViewModel` via constructor injection (`LogStorage`, `NetworkLogStorage`, `EventLogStorage`).
  - [x] Add HAR export option in Network viewer and Events export option in Events screen.
  - [x] Update Settings screen export action to produce full multi-source bundle.
- [x] **Phase 15.5: Hermetic Unit Tests & Verification**
  - [x] Write unit tests for HAR 1.2 export, Events export formats, and decoupled `SettingsViewModelTest`.
  - [x] Run test suite and ktlint validation to ensure zero warnings/errors.

## Phase 16: Crash Reporting & Uncaught Exception Interception

- [x] **Phase 16.1: Core Crash Models, Breadcrumbs & Storage Abstraction**
  - [x] Define `CrashReport`, `Breadcrumb`, and `CrashSeverity` models in `spectra-core`.
  - [x] Implement `CrashStorage` interface with thread-safe `FileCrashStorage` (persisting across process restarts using Okio) and `InMemoryCrashStorage`.
- [x] **Phase 16.2: Platform Uncaught Exception Interceptors**
  - [x] Android: Implement `SpectraCrashHandler` hooking `Thread.setDefaultUncaughtExceptionHandler` chaining original handler.
  - [x] iOS: Implement native uncaught exception and signal interception (`kotlin.native.setUnhandledExceptionHook`).
  - [x] Implement `BreadcrumbRecorder` automatically capturing recent logs, network calls, and UI events into circular ring buffer.
- [x] **Phase 16.3: UI Crash Inspector & Settings Integration**
  - [x] Build `CrashDetailPane` and `CrashHistoryDialog` displaying stack trace, thread info, device state, and breadcrumbs timeline.
  - [x] Wire crash notifications/banners into `spectra-ui` (`CrashBanner`) and `SettingsScreen`.
- [x] **Phase 16.4: Hermetic Unit Tests & Verification**
  - [x] Test crash serialization, breadcrumb buffering, and exception interceptor chaining.
  - [x] Fix GitHub Actions unit test bottleneck (optimized call-stack source detection to prevent Kotlin Native benchmark timeout).
  - [x] Verify zero regressions, 100% test pass rate, and ktlint compliance across all targets.

## Phase 16.5: Clean Architecture, Low Coupling & High Cohesion Telemetry Abstractions

- [x] **Phase 16.5.1: Unified Generic Telemetry Storage Contract**
  - [x] Create `TelemetryStorage<T, in F>` unifying log, network, and event storage under a single generic abstraction (`add`, `addAll`, `query`, `observe`, `count`, `clear`).
  - [x] Specialize `LogStorage`, `NetworkLogStorage`, and `EventLogStorage` conforming to the generic contract with zero-arg default overloads for seamless backward compatibility.
  - [x] Implement optimized batch `addAll` in `InMemoryNetworkLogStorage`.
- [x] **Phase 16.5.2: Complete UI ViewModel Decoupling**
  - [x] Decouple `LogsViewModel`, `StatisticsViewModel`, and `NetworkStatisticsViewModel` via constructor injection of storages/repositories.
  - [x] Ensure 100% of ViewModels in `spectra-ui` adhere to Dependency Inversion with default fallbacks.
- [x] **Phase 16.5.3: Verification & Quality Gate**
  - [x] Verify multiplatform test pass across all targets (`:spectra-core:allTests :spectra-ui:allTests`).
  - [x] Validate ktlint and detekt compliance with 0 errors.

## Phase 17: Log Retention Policies & Auto-Pruning

- [ ] **Phase 17.1: Retention Policy Configuration**
  - [ ] Define `RetentionPolicy` (max log count, max age TTL, max storage size in bytes).
- [ ] **Phase 17.2: Automated Storage Pruning Engine**
  - [ ] Implement TTL-based and size-based eviction in `FileLogStorage` and `InMemoryLogStorage`.
  - [ ] Add manual "Prune Now" action in `SettingsViewModel` and UI.
- [ ] **Phase 17.3: Hermetic Unit Tests & Verification**
  - [ ] Test time-based expiration and capacity eviction.
