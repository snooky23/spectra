# Claude Code Instructions: Spectra Logger

> **Purpose**: This document provides comprehensive instructions for Claude Code when working on the Spectra Logger project. Read this file at the start of every session along with SESSION.md, PLANNING.md, and TASKS.md to understand context.

---

## Core Directives

### 1. Quality Standards
You are building a framework that **must exceed Google's engineering standards**. Every piece of code should be:
- **Production-ready**: No shortcuts, no TODOs, complete implementations
- **Performant**: Meet or exceed all performance targets (see PLANNING.md)
- **Well-tested**: 80%+ test coverage, comprehensive edge case handling
- **Well-documented**: Every public API has clear KDoc with examples
- **Maintainable**: Clean, readable, self-documenting code

### 2. Architecture Principles
**Follow Clean Architecture religiously**:
- **Domain Layer**: Pure business logic, no platform dependencies
- **Data Layer**: Data access, storage, repositories
- **Presentation Layer**: MVVM pattern, UI state management
- **Platform Layer**: expect/actual for platform-specific implementations

**Never violate layer boundaries**. Domain should never import from data or presentation.

### 3. Code Philosophy
- **YAGNI** (You Aren't Gonna Need It): Don't add features not in the spec
- **KISS** (Keep It Simple, Stupid): Simplest solution that works
- **DRY** (Don't Repeat Yourself): Extract common patterns
- **SOLID Principles**: Especially Single Responsibility and Dependency Inversion
- **Composition over Inheritance**: Prefer interfaces and composition

---

## Session Workflow

### Starting a New Session

1. **Read Context Files** (in this order):
   ```
   1. CLAUDE.md (this file) - Instructions
   2. SESSION.md - Current state and progress
   3. TASKS.md - Find current task
   4. PLANNING.md - Understand architecture
   ```

2. **Understand Current State**:
   - What milestone are we on?
   - What tasks are complete?
   - What's next?
   - Any blockers or open questions?

3. **Ask Clarifying Questions** if anything is unclear:
   - Don't make assumptions about requirements
   - Confirm which task to work on if ambiguous
   - Ask about priority if multiple tasks are ready

4. **Plan Before Coding**:
   - Announce what you'll work on
   - Outline your approach
   - Mention any dependencies or prerequisites
   - Estimate complexity (simple/medium/complex)

### During Work

1. **Follow Task Order**: Complete tasks in TASKS.md order unless instructed otherwise

2. **Update SESSION.md** after each significant task or milestone:

   **IMPORTANT**: SESSION.md is the project's memory. Update it frequently to track progress.

   **When to Update**:
   - ✅ After completing a milestone or major feature
   - ✅ After making architectural decisions
   - ✅ After fixing critical bugs
   - ✅ At the end of each coding session
   - ✅ When switching between major tasks

   **What to Update**:
   - **Project Status section**: Update current phase, milestone, progress %
   - **Completed Work section**: Add newly completed features/milestones
   - **Recent Session Work section**: Document what was done in current session
   - **Next Steps section**: Update immediate tasks based on what's left
   - **Last Updated date**: Always update the date at the top of the file
   - **Technical Decisions section**: Document any important choices made

   **How to Update**:
   ```bash
   # 1. After completing a significant task, edit SESSION.md
   # 2. Update the relevant sections (see above)
   # 3. Commit SESSION.md along with code changes
   git add SESSION.md
   git commit -m "Update SESSION.md: [describe what changed]"
   ```

3. **Write Tests First** (TDD when appropriate):
   - Write test for new function
   - Implement function
   - Verify test passes
   - Refactor if needed

4. **Run Code Quality Checks Before Every Commit** ⚠️ **CRITICAL**:
   ```bash
   # ALWAYS run this before committing:
   ./scripts/code-quality.sh

   # This runs:
   # - ktlintCheck (code formatting)
   # - detekt (code quality)
   ```

   **If code quality checks fail**:
   - Fix the issues immediately
   - Run `./gradlew ktlintFormat` to auto-fix formatting
   - Fix detekt violations manually or update baseline if needed
   - Re-run code quality checks until they pass
   - Only then proceed with commit

5. **Commit Frequently**:
   - Small, atomic commits
   - Follow Conventional Commits format
   - Descriptive commit messages
   - Commit after each logical unit of work
   - **Include SESSION.md** in commits when project state changes
   - **Always run code quality checks first** (see step 4 above)

### Ending a Session

1. **Update SESSION.md**:
   - Mark completed tasks
   - Add session notes
   - List any blockers
   - Note what to do next

2. **Leave Clean State**:
   - Code compiles
   - Tests pass
   - No merge conflicts
   - No uncommitted changes (or explicitly noted)

3. **Summarize for User**:
   - What was accomplished
   - What's next
   - Any decisions that need user input

---

## Code Quality Requirements

### Performance

**Logging Performance** (Critical):
```kotlin
// ✅ GOOD: Fast, minimal allocations
fun log(message: String, context: Map<String, Any>) {
    val event = logEventPool.obtain()  // Object pooling if beneficial
    event.populate(timestamp(), message, context)
    storage.add(event)  // O(1) operation
}

// ❌ BAD: Slow, unnecessary work
fun log(message: String, context: Map<String, Any>) {
    val formattedMessage = formatMessage(message)  // Don't format unless needed
    val jsonContext = context.toJson()  // Don't serialize on log thread
    Thread.sleep(10)  // Never block!
    storage.add(LogEvent(formattedMessage, jsonContext))
}
```

**Targets** (from PLANNING.md):
- Log capture: < 0.1ms (critical: < 1ms)
- Network interception: < 5ms (critical: < 20ms)
- UI scroll: 60 FPS
- Memory: < 50MB for 10K logs

**Profile Before Optimizing**:
- Use Android Studio Profiler / Xcode Instruments
- Measure first, then optimize
- Document performance in tests

### Thread Safety

**All logging operations must be thread-safe**:

```kotlin
// ✅ GOOD: Thread-safe with atomic operations
class CircularBuffer<T>(private val capacity: Int) {
    private val buffer = AtomicArray<T?>(capacity)
    private val writeIndex = atomic(0)
    private val size = atomic(0)

    fun add(item: T) {
        val index = writeIndex.getAndIncrement() % capacity
        buffer[index].value = item
        size.updateAndGet { min(it + 1, capacity) }
    }
}

// ❌ BAD: Race conditions
class CircularBuffer<T>(private val capacity: Int) {
    private val buffer = arrayOfNulls<T>(capacity)
    private var writeIndex = 0  // NOT THREAD-SAFE!

    fun add(item: T) {
        buffer[writeIndex] = item
        writeIndex = (writeIndex + 1) % capacity  // RACE CONDITION!
    }
}
```

**Threading Rules**:
- Log capture: **Main thread OK** (must be fast)
- File I/O: **Background thread required**
- Network interception: **Don't block network thread**
- UI updates: **Main thread only**
- Storage operations: **Thread-safe, but preferably off main thread**

### Memory Management

**Use Circular Buffers**:
```kotlin
// ✅ GOOD: Bounded memory, FIFO eviction
class InMemoryStorage(maxSize: Int) {
    private val buffer = CircularBuffer<LogEvent>(maxSize)

    fun add(event: LogEvent) {
        buffer.add(event)  // Automatically evicts oldest
    }
}

// ❌ BAD: Unbounded growth
class InMemoryStorage {
    private val events = mutableListOf<LogEvent>()

    fun add(event: LogEvent) {
        events.add(event)  // Will crash with OOM eventually!
    }
}
```

**Avoid Memory Leaks**:
- Weak references for listeners/callbacks
- Proper cleanup in dispose/deinit
- No static references to Activity/ViewController
- Profile with LeakCanary (Android) / Instruments (iOS)

---

## Architecture Patterns

### Clean Architecture Layers

```
┌─────────────────────────────────────┐
│       Presentation (UI)             │  MVVM pattern
│  - Screens, ViewModels, State       │
└────────────┬────────────────────────┘
             │ calls
┌────────────▼────────────────────────┐
│       Domain (Business Logic)       │  Pure Kotlin
│  - Use Cases, Models, Interfaces    │  No dependencies
└────────────┬────────────────────────┘
             │ depends on abstractions
┌────────────▼────────────────────────┐
│       Data (Persistence)            │  Repository pattern
│  - Repositories, Data Sources       │
└────────────┬────────────────────────┘
             │ uses
┌────────────▼────────────────────────┐
│       Platform (expect/actual)      │  Platform-specific
│  - File I/O, Network, Threading     │
└─────────────────────────────────────┘
```

### File Organization

```kotlin
// Domain Layer - shared/src/commonMain/kotlin/com/spectra/logger/domain/

// 1. Models (data classes, sealed classes)
package com.spectra.logger.domain.model

sealed class LogEvent {
    abstract val id: String
    abstract val timestamp: Instant
    abstract val level: LogLevel
}

data class AppLogEvent(...) : LogEvent()
data class NetworkLogEvent(...) : LogEvent()

// 2. Repository Interfaces (NOT implementations)
package com.spectra.logger.domain.repository

interface LogEventRepository {
    suspend fun getAllLogs(): List<LogEvent>
    suspend fun addLog(event: LogEvent)
    fun observeLogs(): Flow<List<LogEvent>>
}

// 3. Use Cases (one per file, single responsibility)
package com.spectra.logger.domain.usecase

class FilterLogsUseCase(
    private val repository: LogEventRepository
) {
    suspend operator fun invoke(filter: LogFilter): List<LogEvent> {
        val allLogs = repository.getAllLogs()
        return allLogs.filter { filter.matches(it) }
    }
}
```

```kotlin
// Data Layer - shared/src/commonMain/kotlin/com/spectra/logger/data/

// 1. Repository Implementations
package com.spectra.logger.data.repository

class LogEventRepositoryImpl(
    private val inMemorySource: InMemoryDataSource,
    private val fileSource: FileDataSource
) : LogEventRepository {
    override suspend fun getAllLogs() = inMemorySource.getAll()
    override suspend fun addLog(event: LogEvent) = inMemorySource.add(event)
    override fun observeLogs() = inMemorySource.observe()
}

// 2. Data Sources
package com.spectra.logger.data.source

class InMemoryDataSource(maxSize: Int) {
    private val buffer = CircularBuffer<LogEvent>(maxSize)
    private val _events = MutableStateFlow<List<LogEvent>>(emptyList())

    fun add(event: LogEvent) {
        buffer.add(event)
        _events.value = buffer.toList()
    }

    fun observe(): Flow<List<LogEvent>> = _events.asStateFlow()
}
```

```kotlin
// Platform Layer - shared/src/androidMain or iosMain

// expect/actual for platform-specific implementations
package com.spectra.logger.platform

// commonMain:
expect class FileSystem {
    fun writeText(path: String, content: String)
    fun readText(path: String): String
    fun exists(path: String): Boolean
}

// androidMain:
actual class FileSystem(private val context: Context) {
    actual fun writeText(path: String, content: String) {
        val file = File(context.filesDir, path)
        file.writeText(content)
    }
    // ... other methods
}

// iosMain:
actual class FileSystem {
    actual fun writeText(path: String, content: String) {
        val fileManager = NSFileManager.defaultManager
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String
        val filePath = "$documentsDir/$path"
        content.writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding)
    }
    // ... other methods
}
```

### MVVM Pattern for UI

```kotlin
// State: Immutable data class representing UI state
data class LogViewerState(
    val logs: List<LogEvent> = emptyList(),
    val selectedFilters: Set<LogLevel> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// ViewModel: Business logic, state management
class LogViewerViewModel(
    private val filterLogsUseCase: FilterLogsUseCase,
    private val searchLogsUseCase: SearchLogsUseCase,
    private val clearLogsUseCase: ClearLogsUseCase,
    private val exportLogsUseCase: ExportLogsUseCase
) {
    private val _state = MutableStateFlow(LogViewerState())
    val state: StateFlow<LogViewerState> = _state.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val logs = filterLogsUseCase(currentFilter)
                _state.update { it.copy(logs = logs, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onFilterChanged(levels: Set<LogLevel>) {
        _state.update { it.copy(selectedFilters = levels) }
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onClearLogs() {
        viewModelScope.launch {
            clearLogsUseCase()
            loadLogs()
        }
    }

    private fun applyFilters() {
        // Filter logic...
    }
}

// View: Composable (or SwiftUI View) observing state
@Composable
fun LogViewerScreen(viewModel: LogViewerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { LogViewerTopBar(onClearClick = viewModel::onClearLogs) }
    ) {
        Column {
            FilterChipsRow(
                selectedFilters = state.selectedFilters,
                onFilterChanged = viewModel::onFilterChanged
            )

            SearchBar(
                query = state.searchQuery,
                onQueryChanged = viewModel::onSearchQueryChanged
            )

            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.logs.isEmpty()) {
                EmptyState()
            } else {
                LogList(logs = state.logs)
            }
        }
    }
}
```

---

## Testing Standards

### Unit Tests (80%+ Coverage Required)

```kotlin
class CircularBufferTest {
    @Test
    fun `add element within capacity stores element`() {
        val buffer = CircularBuffer<String>(capacity = 3)
        buffer.add("A")

        assertEquals(1, buffer.size)
        assertEquals("A", buffer[0])
    }

    @Test
    fun `add element beyond capacity evicts oldest`() {
        val buffer = CircularBuffer<String>(capacity = 2)
        buffer.add("A")
        buffer.add("B")
        buffer.add("C")  // Should evict "A"

        assertEquals(2, buffer.size)
        assertEquals(listOf("B", "C"), buffer.toList())
    }

    @Test
    fun `concurrent adds are thread-safe`() = runBlocking {
        val buffer = CircularBuffer<Int>(capacity = 1000)
        val jobs = (1..1000).map { i ->
            launch(Dispatchers.Default) {
                buffer.add(i)
            }
        }
        jobs.joinAll()

        assertEquals(1000, buffer.size)
        // All elements should be present, no duplicates
        assertEquals((1..1000).toSet(), buffer.toList().toSet())
    }
}
```

### Integration Tests

```kotlin
class LoggingIntegrationTest {
    @Test
    fun `end to end log capture and retrieval`() = runBlocking {
        // Given
        val config = SpectraConfig(maxInMemoryLogs = 100)
        SpectraLogger.initialize(config)
        val logger = SpectraLogger.getLogger("Test", "app")

        // When
        logger.info("Test message")

        // Then
        val repository = getLogRepository()
        val logs = repository.getAllLogs()
        assertEquals(1, logs.size)
        assertEquals("Test message", logs[0].message)
        assertEquals(LogLevel.INFO, logs[0].level)
    }
}
```

### Performance Tests

```kotlin
class PerformanceTest {
    @Test
    fun `log capture completes under 1ms`() {
        val logger = SpectraLogger.getLogger("Perf", "test")
        val iterations = 1000

        val durations = mutableListOf<Long>()
        repeat(iterations) {
            val start = System.nanoTime()
            logger.info("Performance test message")
            val end = System.nanoTime()
            durations.add(end - start)
        }

        val avgNanos = durations.average()
        val avgMillis = avgNanos / 1_000_000

        assertTrue(
            "Average log capture time ($avgMillis ms) exceeds 1ms threshold",
            avgMillis < 1.0
        )
    }
}
```

---

## Documentation Standards

### Public API Documentation

Every public class, interface, and function **must** have KDoc:

```kotlin
/**
 * Creates a new logger instance for the specified category and subsystem.
 *
 * Loggers are cached internally, so calling this method multiple times with the
 * same category and subsystem will return the same instance.
 *
 * @param category The category for this logger (e.g., "Authentication", "Network").
 *   Used to group related log events.
 * @param subsystem The subsystem identifier (e.g., "app", "sdk", "plugin").
 *   Used to distinguish between different parts of the application.
 * @return A Logger instance configured for the specified category and subsystem.
 *
 * @sample com.spectra.logger.samples.LoggerSamples.basicUsage
 *
 * @see Logger
 * @see SpectraConfig
 * @since 1.0.0
 */
fun getLogger(category: String, subsystem: String): Logger {
    // Implementation...
}
```

### Internal Code Comments

Use comments sparingly for complex logic only:

```kotlin
// ✅ GOOD: Explains WHY, not WHAT
// Use circular buffer to automatically prune old logs when capacity is reached.
// This prevents unbounded memory growth while maintaining recent log history.
private val buffer = CircularBuffer<LogEvent>(config.maxInMemoryLogs)

// ❌ BAD: States the obvious
// This is a circular buffer
private val buffer = CircularBuffer<LogEvent>(config.maxInMemoryLogs)

// ✅ GOOD: Explains non-obvious behavior
// URLProtocol must be registered BEFORE creating URLSession,
// otherwise it won't intercept requests. This is an iOS platform limitation.
URLProtocol.registerClass(SpectraURLProtocol.self)

// ✅ GOOD: Explains performance optimization
// Pre-allocate StringBuilder to avoid multiple allocations during concatenation.
// Measured 30% improvement in log formatting performance.
val builder = StringBuilder(estimatedSize)
```

---

## Common Patterns & Anti-Patterns

### Dependency Injection (Manual)

```kotlin
// ✅ GOOD: Constructor injection with interfaces
class LogViewerViewModel(
    private val filterUseCase: FilterLogsUseCase,
    private val searchUseCase: SearchLogsUseCase
) {
    // Implementation uses injected dependencies
}

// ❌ BAD: Direct instantiation of dependencies
class LogViewerViewModel {
    private val filterUseCase = FilterLogsUseCase(
        LogEventRepositoryImpl(InMemoryDataSource())  // Tight coupling!
    )
}
```

### Error Handling

```kotlin
// ✅ GOOD: Explicit error handling with Result
suspend fun exportLogs(format: ExportFormat): Result<File> {
    return try {
        val logs = repository.getAllLogs()
        val file = when (format) {
            ExportFormat.TEXT -> exportToText(logs)
            ExportFormat.JSON -> exportToJson(logs)
        }
        Result.success(file)
    } catch (e: IOException) {
        Result.failure(ExportException("Failed to export logs", e))
    }
}

// ❌ BAD: Swallowing exceptions
suspend fun exportLogs(format: ExportFormat): File? {
    try {
        // ... export logic
    } catch (e: Exception) {
        return null  // What went wrong? User doesn't know!
    }
}
```

### Immutability

```kotlin
// ✅ GOOD: Immutable data class
data class LogEvent(
    val id: String,
    val timestamp: Instant,
    val level: LogLevel,
    val message: String,
    val context: Map<String, Any>
)

// ❌ BAD: Mutable properties
data class LogEvent(
    var id: String,
    var timestamp: Instant,
    var level: LogLevel,
    var message: String
)
```

### Resource Management

```kotlin
// ✅ GOOD: Use 'use' for automatic resource cleanup
fun readLogFile(path: String): String {
    return File(path).bufferedReader().use { reader ->
        reader.readText()
    }
}

// ❌ BAD: Manual resource management (easy to leak)
fun readLogFile(path: String): String {
    val reader = File(path).bufferedReader()
    val content = reader.readText()
    reader.close()  // What if readText() throws?
    return content
}
```

---

## Git & Version Control

### Commit Messages (Conventional Commits)

```bash
# ✅ GOOD: Clear, structured commit messages
feat(core): implement LogManager singleton with logger registry
fix(android): resolve thread safety issue in OkHttpInterceptor
docs(readme): add installation instructions for iOS
test(domain): add unit tests for FilterLogsUseCase
refactor(data): extract CircularBuffer to separate file
chore(deps): update kotlinx.coroutines to 1.7.3

# ❌ BAD: Vague or unhelpful messages
Update code
Fix bug
WIP
asdf
```

### Branch Strategy

```bash
# Feature branches
feature/milestone-1.1-project-setup
feature/milestone-1.2-core-logging
feature/network-logging-android

# Bug fixes
bugfix/circular-buffer-thread-safety
bugfix/ios-framework-build

# Hot fixes (critical bugs in released version)
hotfix/memory-leak-in-logger

# Release branches
release/1.0.0
release/1.1.0
```

### Pull Request Guidelines

1. **Title**: Clear, descriptive (e.g., "Implement network logging for Android")
2. **Description**:
   - What changed
   - Why it changed
   - Testing done
   - Breaking changes (if any)
3. **Checklist**:
   - [ ] Code follows style guide
   - [ ] Tests added/updated
   - [ ] Documentation updated
   - [ ] Builds pass
   - [ ] Manually tested

### Maintaining CHANGELOG.md

**IMPORTANT**: We maintain a CHANGELOG.md file following [Keep a Changelog](https://keepachangelog.com/) format.

**When to Update CHANGELOG.md**:
- ✅ **After adding a new feature** (feat: commits)
- ✅ **After fixing a bug** (fix: commits)
- ✅ **After making breaking changes** (BREAKING CHANGE)
- ✅ **After deprecating functionality**
- ✅ **When preparing a release** (consolidate all changes)
- ❌ **NOT for internal refactoring** (unless it affects users)
- ❌ **NOT for documentation updates** (unless significant)
- ❌ **NOT for chore/build commits** (unless user-visible)

**How to Update**:

1. **Add entry to [Unreleased] section** during development:
   ```markdown
   ## [Unreleased]

   ### Added
   - New feature X that allows users to Y

   ### Fixed
   - Bug where Z caused crash in edge case

   ### Changed
   - Improved performance of W by 50%
   ```

2. **When releasing**, move [Unreleased] to versioned section:
   ```markdown
   ## [Unreleased]

   <!-- Empty until next development cycle -->

   ## [1.1.0] - 2025-10-15

   ### Added
   - New feature X that allows users to Y

   ### Fixed
   - Bug where Z caused crash in edge case
   ```

3. **Update links at bottom**:
   ```markdown
   [Unreleased]: https://github.com/snooky23/Spectra/compare/v1.1.0...HEAD
   [1.1.0]: https://github.com/snooky23/Spectra/compare/v1.0.0...v1.1.0
   ```

**Categories** (in this order):
- **Added** - New features
- **Changed** - Changes in existing functionality
- **Deprecated** - Soon-to-be removed features
- **Removed** - Removed features
- **Fixed** - Bug fixes
- **Security** - Security fixes

**Example Workflow**:

```bash
# 1. Make code changes
git add MyNewFeature.kt
git commit -m "feat(core): add automatic log rotation"

# 2. Update CHANGELOG.md
# Add entry under [Unreleased] > Added:
# - Automatic log rotation with configurable size and count limits

# 3. Commit CHANGELOG
git add CHANGELOG.md
git commit -m "docs(changelog): add automatic log rotation entry"
```

**Release Process**:
When creating a release (e.g., v1.1.0):
1. Move all [Unreleased] entries to new version section
2. Add release date
3. Update comparison links
4. Commit: `git commit -m "docs(changelog): prepare v1.1.0 release"`

---

## Platform-Specific Guidance

### Android-Specific

**Context Handling**:
```kotlin
// ✅ GOOD: Application context, no memory leaks
class FileSystemAndroid(context: Context) {
    private val appContext = context.applicationContext

    fun writeFile(name: String, content: String) {
        val file = File(appContext.filesDir, name)
        file.writeText(content)
    }
}

// ❌ BAD: Activity context can cause memory leaks
class FileSystemAndroid(private val activity: Activity) {
    // Holding Activity reference can prevent GC!
}
```

**OkHttp Interceptor**:
```kotlin
class SpectraInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        // Log request
        val requestLog = captureRequest(request)

        // Execute request
        val response = chain.proceed(request)

        // Log response
        val duration = System.nanoTime() - startTime
        val responseLog = captureResponse(response, duration)

        // Combine and log
        LogManager.addNetworkLog(requestLog, responseLog)

        return response
    }
}
```

### iOS-Specific

**URLProtocol Registration**:
```kotlin
// In iosMain
actual fun registerNetworkInterceptor() {
    URLProtocol.registerClass(SpectraURLProtocol)
}

// Must be called before creating URLSession!
// In Swift wrapper:
SpectraLogger.registerNetworkInterceptor()
let session = URLSession(configuration: .default)
```

**File System**:
```kotlin
actual class FileSystemIos {
    private val fileManager = NSFileManager.defaultManager
    private val documentsDir: String
        get() = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String

    actual fun writeText(path: String, content: String) {
        val fullPath = "$documentsDir/$path"
        content.writeToFile(
            fullPath,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
    }
}
```

### KMP-Specific

**expect/actual Pattern**:
```kotlin
// commonMain - declare expected API
expect class PlatformLogger {
    fun logToSystemConsole(message: String)
}

// androidMain - provide Android implementation
actual class PlatformLogger {
    actual fun logToSystemConsole(message: String) {
        android.util.Log.d("Spectra", message)
    }
}

// iosMain - provide iOS implementation
actual class PlatformLogger {
    actual fun logToSystemConsole(message: String) {
        NSLog(message)
    }
}
```

---

## Common Pitfalls to Avoid

### 1. Breaking Layer Boundaries
```kotlin
// ❌ NEVER: Domain depending on Data or Platform
// domain/usecase/FilterLogsUseCase.kt
import com.spectra.logger.data.source.InMemoryDataSource  // NO!
import android.content.Context  // DEFINITELY NO!

// ✅ CORRECT: Domain depends only on abstractions
import com.spectra.logger.domain.repository.LogEventRepository  // YES
```

### 2. Blocking the Main Thread
```kotlin
// ❌ BAD: File I/O on main thread
fun saveLog(event: LogEvent) {
    File("log.txt").appendText(event.toString())  // Blocks!
}

// ✅ GOOD: Background thread for I/O
suspend fun saveLog(event: LogEvent) = withContext(Dispatchers.IO) {
    File("log.txt").appendText(event.toString())
}
```

### 3. Memory Leaks
```kotlin
// ❌ BAD: Listener not cleaned up
class LogManager {
    private val listeners = mutableListOf<LogListener>()

    fun addListener(listener: LogListener) {
        listeners.add(listener)  // Never removed!
    }
}

// ✅ GOOD: Weak references or explicit removal
class LogManager {
    private val listeners = mutableListOf<WeakReference<LogListener>>()

    fun addListener(listener: LogListener) {
        listeners.add(WeakReference(listener))
        cleanupStaleListeners()
    }

    private fun cleanupStaleListeners() {
        listeners.removeAll { it.get() == null }
    }
}
```

### 4. Not Handling Exceptions
```kotlin
// ❌ BAD: Uncaught exception crashes app
fun parseLogEvent(json: String): LogEvent {
    return Json.decodeFromString(json)  // Can throw!
}

// ✅ GOOD: Explicit error handling
fun parseLogEvent(json: String): Result<LogEvent> {
    return try {
        Result.success(Json.decodeFromString(json))
    } catch (e: SerializationException) {
        Result.failure(ParseException("Invalid log event JSON", e))
    }
}
```

### 5. Over-Engineering
```kotlin
// ❌ BAD: Unnecessary abstraction
interface LogEventFactory {
    fun create(timestamp: Instant, level: LogLevel, message: String): LogEvent
}

class LogEventFactoryImpl : LogEventFactory {
    override fun create(timestamp: Instant, level: LogLevel, message: String) =
        AppLogEvent(UUID.random(), timestamp, level, message)
}

// ✅ GOOD: Simple and direct
fun createLogEvent(timestamp: Instant, level: LogLevel, message: String) =
    AppLogEvent(UUID.random(), timestamp, level, message)
```

---

## When You're Stuck

### Debugging Checklist

1. **Read the error message carefully**: Often contains the answer
2. **Check SESSION.md**: Has this been solved before?
3. **Check PLANNING.md**: Is the architecture clear?
4. **Review tests**: What behavior is expected?
5. **Simplify**: Can you reproduce with minimal code?
6. **Profile**: Is it a performance issue? Measure!
7. **Ask the user**: When truly stuck, ask for clarification

### Getting Unstuck

```markdown
**Problem**: [Clear description of issue]

**What I Tried**:
1. [First attempt]
2. [Second attempt]
3. [Third attempt]

**Current Understanding**:
[What you think is happening]

**Question**:
[Specific question for the user]
```

---

## Success Criteria

You'll know you've done well when:

1. ✅ **All tests pass** (green build)
2. ✅ **Code coverage ≥ 80%** for new code
3. ✅ **Performance targets met** (< 0.1ms log capture, etc.)
4. ✅ **No compiler warnings** (treat warnings as errors)
5. ✅ **Documentation complete** (all public APIs have KDoc)
6. ✅ **Code reviewed** (by yourself or peer)
7. ✅ **User can understand and use** your code
8. ✅ **You're proud of the result** (would you use this yourself?)

---

## Final Reminders

- **Read SESSION.md first** - It has current context
- **Follow TASKS.md order** - Tasks are sequenced for a reason
- **Reference PLANNING.md** - Architecture decisions are documented
- **Update SESSION.md** - Track progress and decisions
- **Test everything** - No untested code in production
- **Ask questions** - When in doubt, ask the user
- **Be proud** - This is a professional-grade framework

**You are building something great. Let's make it world-class!** 🚀

---

**Document Version**: 2.0 (Instructions-only)
**Last Updated**: 2025-10-03
**Companion File**: SESSION.md (for session memory)
