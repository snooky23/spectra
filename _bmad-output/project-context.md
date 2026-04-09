---
project_name: 'Spectra'
user_name: 'Avi'
date: '2026-04-08'
sections_completed: ['technology_stack']
existing_patterns_found: 12
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

- **Kotlin**: 2.2.10 (K2 compiler)
- **Compose Multiplatform**: 1.10.3
- **Kotlin Coroutines**: 1.10.1
- **Kotlin Serialization**: 1.8.0
- **Ktor Client**: 3.0.3
- **SKIE (Swift-Kotlin Interface Enhancer)**: 0.10.11
- **Material 3 Adaptive**: 1.3.0-alpha03
- **Material 3 Adaptive Navigation Suite**: 1.11.0-alpha05
- **Android Gradle Plugin (AGP)**: 9.1.0
- **Ktlint**: 12.1.0
- **Detekt**: 1.23.5

## Critical Implementation Rules

- **Clean Architecture Enforcement**: Strictly separate `spectra-core` (Domain, Data, Storage) from `spectra-ui` (Presentation, Compose).
- **KMP Multiplatform Structure**: Prioritize `commonMain` for logic. Use `expect`/`actual` sparingly; prefer dependency injection of platform-specific implementations.
- **UI State Management**: Use ViewModels from `androidx.lifecycle:lifecycle-viewmodel` (KMP) to hold UI state via `MutableStateFlow`.
- **Adaptive Layouts**: Always use Material 3 Adaptive components (`NavigationSuiteScaffold`, `ListDetailPaneScaffold`) for layout consistency across Android and iOS (iPhone/iPad).
- **Async & Threading**: Use Kotlin Coroutines (`viewModelScope`, `IO` dispatcher) for all non-blocking operations.
- **Coding Style**: Adhere to Kotlin 2.x idioms. Ensure Ktlint/Detekt compliance.
- **Documentation**: Use KDoc for all public APIs. Include usage examples in main entry point classes.
