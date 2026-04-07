# Spectra Logger - Technical Planning Document

## Vision

Create the **definitive cross-platform mobile logging solution** that works seamlessly in native iOS, native Android, and KMP projects with a unified API, on-device debugging UI, and zero external dependencies.

### Design Principles

1. **Performance First**: Logging should never impact app performance
2. **Zero Config**: Works out of the box with sensible defaults
3. **Platform Native**: Feels natural on each platform while sharing logic
4. **Developer Joy**: Simple API, great debugging experience
5. **Production Ready**: Battle-tested, well-documented, thoroughly tested

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HOST APPLICATION                         │
│  (Native iOS / Native Android / KMP Project)               │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Public API
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                 SPECTRA LOGGER FRAMEWORK                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐      ┌────────────────────────┐      │
│  │  Presentation   │      │   Mobile UI Screens     │      │
│  │     Layer       │◄────►│  - Log Viewer          │      │
│  │  (UI/ViewModels)│      │  - Network Viewer      │      │
│  └────────┬────────┘      │  - Settings            │      │
│           │               └────────────────────────┘      │
│           │                                               │
│  ┌────────▼────────┐                                      │
│  │  Domain Layer   │                                      │
│  │  (Use Cases)    │                                      │
│  │  - LogUseCase   │                                      │
│  │  - FilterUseCase│                                      │
│  │  - ExportUseCase│                                      │
│  └────────┬────────┘                                      │
│           │                                               │
│  ┌────────▼────────────────────────────────┐             │
│  │         Data Layer                      │             │
│  │  ┌──────────────────────────────────┐   │             │
│  │  │  LogEventRepository              │   │             │
│  │  │  - Query logs                    │   │             │
│  │  │  - Filter / Search               │   │             │
│  │  │  - Export                        │   │             │
│  │  └──────────┬───────────────────────┘   │             │
│  │             │                            │             │
│  │  ┌──────────▼───────────────────────┐   │             │
│  │  │  Storage Layer                   │   │             │
│  │  │  - InMemoryStorage (Circular)    │   │             │
│  │  │  - FileStorage (expect/actual)   │   │             │
│  │  └──────────────────────────────────┘   │             │
│  └─────────────────────────────────────────┘             │
│                                                           │
│  ┌───────────────────────────────────────────────────┐   │
│  │         Core Logging Engine                       │   │
│  │  ┌────────────┐    ┌─────────────┐               │   │
│  │  │ LogManager │───►│   Storage   │               │   │
│  │  │ (Singleton)│    │  Dispatcher │               │   │
│  │  └──────┬─────┘    └─────────────┘               │   │
│  │         │                                          │   │
│  │  ┌──────▼─────┐                                   │   │
│  │  │   Logger   │ (Per category/subsystem)          │   │
│  │  │  Instances │                                   │   │
│  │  └────────────┘                                   │   │
│  └───────────────────────────────────────────────────┘   │
│                                                           │
│  ┌───────────────────────────────────────────────────┐   │
│  │      Network Interception Layer                   │   │
│  │  ┌────────────────┐  ┌──────────────────────┐    │   │
│  │  │ OkHttp         │  │ URLProtocol          │    │   │
│  │  │ Interceptor    │  │ (iOS)                │    │   │
│  │  │ (Android)      │  └──────────────────────┘    │   │
│  │  └────────────────┘                               │   │
│  │  ┌────────────────┐                               │   │
│  │  │ Ktor Plugin    │                               │   │
│  │  │ (KMP)          │                               │   │
│  │  └────────────────┘                               │   │
│  └───────────────────────────────────────────────────┘   │
│                                                           │
│  ┌───────────────────────────────────────────────────┐   │
│  │        Platform Abstractions                      │   │
│  │   (expect/actual implementations)                 │   │
│  │  - File I/O                                       │   │
│  │  - Threading                                      │   │
│  │  - Date/Time                                      │   │
│  │  - UI Presentation                                │   │
│  └───────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Clean Architecture Layers

#### 1. Presentation Layer (UI)
**Responsibility**: Display logs to users, handle user interactions

**Components**:
- **Screens/Views**: SwiftUI (iOS), Jetpack Compose (Android), or Compose Multiplatform
- **ViewModels**: State management, UI logic
- **State Classes**: Immutable UI state
- **Navigation**: Screen routing

**Pattern**: MVVM (Model-View-ViewModel)

```
View (Composable/SwiftUI) ◄──► ViewModel ◄──► Use Cases
                                    │
                                    ├── UIState
                                    └── Events
```

#### 2. Domain Layer (Business Logic)
**Responsibility**: Core business rules, platform-agnostic

**Components**:
- **Use Cases**: Single-responsibility operations
  - `LogEventUseCase`: Create and store log events
  - `FilterLogsUseCase`: Apply filters to logs
  - `SearchLogsUseCase`: Search log messages
  - `ExportLogsUseCase`: Export logs to file
  - `ClearLogsUseCase`: Clear stored logs
- **Domain Models**: Pure Kotlin data classes
- **Repository Interfaces**: Abstract data access

**Location**: `shared/src/commonMain/kotlin/com/spectra/logger/domain/`

#### 3. Data Layer
**Responsibility**: Data access, storage management

**Components**:
- **Repositories**: Implement domain interfaces
  - `LogEventRepository`: Access to app/network logs
  - `ConfigRepository`: Access to settings
- **Data Sources**:
  - `InMemoryDataSource`: Circular buffer storage
  - `FileDataSource`: Persistent file storage (expect/actual)
- **DTOs**: Data transfer objects (if needed)

**Location**: `shared/src/commonMain/kotlin/com/spectra/logger/data/`

#### 4. Platform Layer
**Responsibility**: Platform-specific implementations

**Components**:
- File I/O (expect/actual)
- Network interception (platform-specific)
- Threading/Coroutines dispatchers
- UI presentation (platform-specific)

**Location**:
- `shared/src/androidMain/kotlin/com/spectra/logger/platform/`
- `shared/src/iosMain/kotlin/com/spectra/logger/platform/`

### Module Structure

```
Spectra/
├── shared/                              # Core KMP module
│   ├── src/
│   │   ├── commonMain/kotlin/com/spectra/logger/
│   │   │   ├── SpectraLogger.kt        # Public API entry point
│   │   │   ├── SpectraConfig.kt        # Configuration
│   │   │   │
│   │   │   ├── core/                   # Core logging engine
│   │   │   │   ├── LogManager.kt       # Central manager (singleton)
│   │   │   │   ├── Logger.kt           # Logger instances
│   │   │   │   ├── LogLevel.kt         # Log levels enum
│   │   │   │   └── LogContext.kt       # Context management
│   │   │   │
│   │   │   ├── domain/                 # Business logic
│   │   │   │   ├── model/              # Domain models
│   │   │   │   │   ├── LogEvent.kt     # Sealed class
│   │   │   │   │   ├── AppLogEvent.kt
│   │   │   │   │   ├── NetworkLogEvent.kt
│   │   │   │   │   └── LogFilter.kt
│   │   │   │   │
│   │   │   │   ├── repository/         # Repository interfaces
│   │   │   │   │   ├── LogEventRepository.kt
│   │   │   │   │   └── ConfigRepository.kt
│   │   │   │   │
│   │   │   │   └── usecase/            # Use cases
│   │   │   │       ├── LogEventUseCase.kt
│   │   │   │       ├── FilterLogsUseCase.kt
│   │   │   │       ├── SearchLogsUseCase.kt
│   │   │   │       ├── ExportLogsUseCase.kt
│   │   │   │       └── ClearLogsUseCase.kt
│   │   │   │
│   │   │   ├── data/                   # Data layer
│   │   │   │   ├── repository/         # Repository implementations
│   │   │   │   │   ├── LogEventRepositoryImpl.kt
│   │   │   │   │   └── ConfigRepositoryImpl.kt
│   │   │   │   │
│   │   │   │   ├── source/             # Data sources
│   │   │   │   │   ├── InMemoryDataSource.kt
│   │   │   │   │   ├── FileDataSource.kt
│   │   │   │   │   └── CircularBuffer.kt
│   │   │   │   │
│   │   │   │   └── mapper/             # Data mappers (if needed)
│   │   │   │
│   │   │   ├── network/                # Network logging
│   │   │   │   ├── NetworkInterceptor.kt  # Interface
│   │   │   │   ├── NetworkLogParser.kt
│   │   │   │   └── HeaderRedactor.kt
│   │   │   │
│   │   │   ├── platform/               # Platform abstractions
│   │   │   │   ├── FileSystem.kt       # expect/actual
│   │   │   │   ├── CurrentTime.kt      # expect/actual
│   │   │   │   └── Threading.kt        # Dispatchers
│   │   │   │
│   │   │   └── util/                   # Utilities
│   │   │       ├── Extensions.kt
│   │   │       └── Serialization.kt
│   │   │
│   │   ├── commonTest/                 # Shared tests
│   │   │
│   │   ├── androidMain/kotlin/com/spectra/logger/
│   │   │   ├── platform/               # Android implementations
│   │   │   │   ├── FileSystemAndroid.kt
│   │   │   │   └── CurrentTimeAndroid.kt
│   │   │   │
│   │   │   └── network/                # Android network
│   │   │       └── OkHttpInterceptor.kt
│   │   │
│   │   ├── iosMain/kotlin/com/spectra/logger/
│   │   │   ├── platform/               # iOS implementations
│   │   │   │   ├── FileSystemIos.kt
│   │   │   │   └── CurrentTimeIos.kt
│   │   │   │
│   │   │   └── network/                # iOS network
│   │   │       └── URLProtocolWrapper.kt
│   │   │
│   │   └── build.gradle.kts
│   │
│   └── ui/                             # Optional: Compose Multiplatform UI
│       ├── src/
│       │   ├── commonMain/kotlin/com/spectra/logger/ui/
│       │   │   ├── screen/             # Screens
│       │   │   │   ├── logviewer/
│       │   │   │   │   ├── LogViewerScreen.kt
│       │   │   │   │   ├── LogViewerViewModel.kt
│       │   │   │   │   └── LogViewerState.kt
│       │   │   │   │
│       │   │   │   ├── networkviewer/
│       │   │   │   │   ├── NetworkViewerScreen.kt
│       │   │   │   │   ├── NetworkViewerViewModel.kt
│       │   │   │   │   └── NetworkViewerState.kt
│       │   │   │   │
│       │   │   │   ├── settings/
│       │   │   │   │   ├── SettingsScreen.kt
│       │   │   │   │   ├── SettingsViewModel.kt
│       │   │   │   │   └── SettingsState.kt
│       │   │   │   │
│       │   │   │   └── detail/
│       │   │   │       ├── LogDetailScreen.kt
│       │   │   │       └── NetworkDetailScreen.kt
│       │   │   │
│       │   │   ├── component/          # Reusable components
│       │   │   │   ├── LogListItem.kt
│       │   │   │   ├── FilterChip.kt
│       │   │   │   ├── SearchBar.kt
│       │   │   │   └── EmptyState.kt
│       │   │   │
│       │   │   ├── navigation/         # Navigation
│       │   │   │   └── NavGraph.kt
│       │   │   │
│       │   │   └── theme/              # Theme
│       │   │       ├── Color.kt
│       │   │       ├── Typography.kt
│       │   │       └── Theme.kt
│       │   │
│       │   └── build.gradle.kts
│       │
├── examples/                           # Example applications
│   ├── android-native/                 # Native Android example
│   ├── ios-native/                     # Native iOS example
│   └── kmp-app/                        # KMP example
│
├── docs/                               # Documentation
│   ├── getting-started.md
│   ├── integration-guide-android.md
│   ├── integration-guide-ios.md
│   ├── integration-guide-kmp.md
│   ├── api-reference.md
│   └── architecture.md
│
└── build.gradle.kts                    # Root build file
```

## Technology Stack

### Core Framework (Shared Module)

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 1.9.22 | Primary language |
| **Kotlin Multiplatform** | 1.9.22 | Cross-platform code sharing |
| **kotlinx.coroutines** | 1.7.3 | Async/concurrent operations |
| **kotlinx.serialization** | 1.6.2 | JSON serialization for export |
| **kotlinx.datetime** | 0.5.0 | Cross-platform date/time |
| **kotlinx.atomicfu** | 0.23.1 | Thread-safe atomic operations |

### UI Layer (Choose One)

#### Option A: Compose Multiplatform (Recommended)
| Technology | Version | Purpose |
|------------|---------|---------|
| **Compose Multiplatform** | 1.5.11 | Shared UI across platforms |
| **Compose Material 3** | 1.1.2 | Material Design components |
| **Compose Navigation** | 2.7.6 | Screen navigation |

#### Option B: Native UI
| Technology | Version | Purpose |
|------------|---------|---------|
| **Jetpack Compose** (Android) | 1.5.4 | Android UI |
| **SwiftUI** (iOS) | iOS 15+ | iOS UI |

### Platform-Specific

#### Android
| Technology | Version | Purpose |
|------------|---------|---------|
| **Android Gradle Plugin** | 8.2.0 | Build system |
| **OkHttp** | 4.12.0 | HTTP client (for interception) |
| **AndroidX Core KTX** | 1.12.0 | Android utilities |

#### iOS
| Technology | Version | Purpose |
|------------|---------|---------|
| **Xcode** | 15+ | iOS development |
| **Foundation** | iOS 13+ | Core iOS APIs |

#### KMP Projects (Ktor Support)
| Technology | Version | Purpose |
|------------|---------|---------|
| **Ktor Client** | 2.3.7 | HTTP client for network logging |

### Build & Deployment

| Technology | Version | Purpose |
|------------|---------|---------|
| **Gradle** | 8.5 | Build automation |
| **Gradle Kotlin DSL** | - | Build configuration |
| **Dokka** | 1.9.10 | API documentation generation |
| **Maven Publish Plugin** | - | Artifact publishing |

### Testing

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin Test** | 1.9.22 | Common test framework |
| **JUnit 5** | 5.10.1 | Android unit tests |
| **Kotest** | 5.8.0 | Additional assertions |
| **MockK** | 1.13.8 | Mocking framework |
| **Turbine** | 1.0.0 | Flow testing |

### CI/CD

| Technology | Purpose |
|------------|---------|
| **GitHub Actions** | CI/CD pipeline |
| **ktlint** | Code formatting |
| **detekt** | Static code analysis |

## Required Tools

### Development Environment

#### For All Developers
- **IntelliJ IDEA Ultimate** or **Android Studio Hedgehog+**
- **JDK 17** (recommended: Azul Zulu or OpenJDK)
- **Git** 2.40+
- **Kotlin 1.9.22+**

#### For Android Development
- **Android Studio** Hedgehog (2023.1.1) or later
- **Android SDK** 34
- **Android NDK** (if needed for native code)

#### For iOS Development
- **macOS** 13+ (Ventura or Sonoma)
- **Xcode** 15+

### Build Tools
```bash
# Install Homebrew (macOS)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"


# Install Kotlin compiler (optional, Gradle will handle it)
brew install kotlin

# Install ktlint
brew install ktlint

# Install detekt
brew install detekt
```

### Recommended IDE Plugins
- **Kotlin Multiplatform Mobile** (for Android Studio)
- **Compose Multiplatform IDE Support**
- **Detekt** (code quality)
- **Rainbow Brackets** (readability)
- **GitToolBox** (git integration)

## Design Patterns

### 1. Singleton (LogManager)
```kotlin
object LogManager {
    private val loggers = mutableMapOf<String, Logger>()
    // ...
}
```

### 2. Factory Pattern (Logger Creation)
```kotlin
fun getLogger(category: String, subsystem: String): Logger {
    return loggers.getOrPut("$category.$subsystem") {
        Logger(category, subsystem, config)
    }
}
```

### 3. Repository Pattern (Data Access)
```kotlin
interface LogEventRepository {
    suspend fun getAllLogs(): List<LogEvent>
    suspend fun filterLogs(filter: LogFilter): List<LogEvent>
    suspend fun addLog(event: LogEvent)
}
```

### 4. Observer Pattern (Log Updates)
```kotlin
interface LogEventListener {
    fun onLogAdded(event: LogEvent)
}
```

### 5. Strategy Pattern (Filters)
```kotlin
sealed class LogFilter {
    data class ByLevel(val levels: Set<LogLevel>) : LogFilter()
    data class ByText(val query: String) : LogFilter()
    data class ByTime(val start: Instant, val end: Instant) : LogFilter()
}
```

### 6. Builder Pattern (Configuration)
```kotlin
val config = SpectraConfig.Builder()
    .maxInMemoryLogs(5000)
    .enableFileLogging(true)
    .enableNetworkLogging(true)
    .build()
```

### 7. Dependency Injection (Manual)
```kotlin
class LogViewerViewModel(
    private val filterLogsUseCase: FilterLogsUseCase,
    private val searchLogsUseCase: SearchLogsUseCase,
    private val exportLogsUseCase: ExportLogsUseCase
)
```

## Performance Considerations

### 1. Circular Buffer for Memory Management
```kotlin
class CircularBuffer<T>(private val capacity: Int) {
    private val buffer = arrayOfNulls<Any?>(capacity)
    private var writeIndex = 0
    private var size = 0

    fun add(item: T) {
        buffer[writeIndex] = item
        writeIndex = (writeIndex + 1) % capacity
        if (size < capacity) size++
    }
}
```

### 2. Background Threading
- Log capture: Main thread (must be fast)
- File I/O: Background thread
- Export: Background thread
- UI updates: Main thread

### 3. Lazy Loading in UI
```kotlin
@Composable
fun LogList(logs: List<LogEvent>) {
    LazyColumn {
        items(logs, key = { it.id }) { log ->
            LogListItem(log)
        }
    }
}
```

### 4. Flow for Reactive Updates
```kotlin
class LogEventRepository {
    private val _logs = MutableStateFlow<List<LogEvent>>(emptyList())
    val logs: StateFlow<List<LogEvent>> = _logs.asStateFlow()
}
```

### 5. Memory Limits
- Default: 10,000 events in memory (~40MB)
- Configurable: 1,000 - 50,000 events
- File logging: Optional, with size limits

## Security Considerations

1. **Header Redaction**: Automatically redact sensitive headers (Authorization, Cookie)
2. **Body Sanitization**: Option to exclude request/response bodies
3. **Debug-Only UI**: UI only accessible in debug builds by default
4. **Local Storage**: All logs stored locally in app sandbox
5. **Export Warning**: Warn users before exporting logs with sensitive data
6. **No Analytics**: No telemetry or data collection by default

## Versioning Strategy

- **Semantic Versioning**: MAJOR.MINOR.PATCH
- **API Stability**: Follow Kotlin's evolution rules
- **Deprecation Policy**: 2 minor versions before removal
- **Beta Releases**: 0.x.x versions
- **Stable Releases**: 1.0.0+

## Release Artifacts

### Maven Central
```
com.spectra:logger:1.0.0              # Core KMP module
com.spectra:logger-android:1.0.0     # Android-specific AAR
```

```ruby
```

### GitHub Releases
- Source code (zip/tar.gz)
- XCFramework (direct download)
- AAR (direct download)
- Release notes

---

**Document Version**: 1.0
**Last Updated**: 2025-10-03
**Status**: Ready for Implementation
