# Spectra Logger - Usage Guide

Step-by-step guide for integrating and using Spectra Logger in your mobile applications.

## Table of Contents

1. [Installation](#installation)
2. [Quick Start](#quick-start)
3. [Platform-Specific Setup](#platform-specific-setup)
4. [Common Use Cases](#common-use-cases)
5. [Advanced Features](#advanced-features)
6. [UI Integration](#ui-integration)

---

## Installation

### Gradle (Kotlin Multiplatform)

Add to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.spectra.logger:shared:0.0.1-SNAPSHOT")
        }
    }
}
```

### Gradle (Android Only)

```kotlin
dependencies {
    implementation("com.spectra.logger:shared:0.0.1-SNAPSHOT")
}
```

### CocoaPods (iOS)

```ruby
pod 'SpectraLogger', '~> 0.0.1'
```

---

## Quick Start

### 1. Initialize the Logger

**Android:**
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SpectraLogger.configure {
            minLogLevel = LogLevel.VERBOSE
        }
    }
}
```

**iOS (Swift):**
```swift
@main
struct MyApp: App {
    init() {
        SpectraLogger.shared.configure { config in
            config.minLogLevel = LogLevel.verbose
        }
    }
}
```

### 2. Start Logging

```kotlin
SpectraLogger.i("App", "Application started")
SpectraLogger.d("Network", "API call initiated")
SpectraLogger.w("Performance", "Slow operation detected")
```

### 3. View Logs (Optional)

Add the log viewer to your app:

```kotlin
@Composable
fun DebugScreen() {
    LogListScreen(storage = SpectraLogger.logStorage)
}
```

---

## Platform-Specific Setup

### Android Native Integration

#### 1. Add Dependency

`build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":shared"))
    // or
    implementation("com.spectra.logger:shared:0.0.1-SNAPSHOT")
}
```

#### 2. Initialize in Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        SpectraLogger.configure {
            minLogLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.INFO

            logStorage {
                maxCapacity = 20_000
                enablePersistence = true
            }

            networkStorage {
                maxCapacity = 2_000
            }

            features {
                enableNetworkLogging = true
            }
        }

        // Optional: Log app lifecycle
        SpectraLogger.i("App", "Application created")
    }
}
```

#### 3. Add to AndroidManifest.xml

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

#### 4. Use in Activities/Fragments

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SpectraLogger.d("MainActivity", "onCreate called")

        setContent {
            MyAppTheme {
                // Your content
            }
        }
    }
}
```

---

### iOS Native Integration

#### 1. Link Framework

Build the framework:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Add to Xcode:
- Drag `shared.framework` to your project
- Add to "Frameworks, Libraries, and Embedded Content"
- Set to "Embed & Sign"

#### 2. Import in Swift

```swift
import shared
```

#### 3. Configure in App

```swift
@main
struct MyiOSApp: App {
    init() {
        configureLogger()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func configureLogger() {
        SpectraLogger.shared.configure { config in
            #if DEBUG
            config.minLogLevel = LogLevel.verbose
            #else
            config.minLogLevel = LogLevel.warning
            #endif

            config.logStorage { storage in
                storage.maxCapacity = 20000
                storage.enablePersistence = true
            }

            config.networkStorage { storage in
                storage.maxCapacity = 2000
            }

            config.features { features in
                features.enableNetworkLogging = true
            }
        }

        SpectraLogger.shared.i(tag: "App", message: "iOS app initialized")
    }
}
```

#### 4. Use in Views

```swift
struct ContentView: View {
    var body: some View {
        Button("Click Me") {
            SpectraLogger.shared.d(tag: "UI", message: "Button tapped")
        }
        .onAppear {
            SpectraLogger.shared.v(tag: "UI", message: "ContentView appeared")
        }
    }
}
```

---

### Kotlin Multiplatform Integration

#### 1. Add to Shared Module

`shared/build.gradle.kts`:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.spectra.logger:shared:0.0.1-SNAPSHOT")
        }
    }
}
```

#### 2. Create Shared Logger

`shared/src/commonMain/kotlin/Logger.kt`:
```kotlin
object AppLogger {
    fun initialize() {
        SpectraLogger.configure {
            minLogLevel = LogLevel.VERBOSE
            logStorage {
                maxCapacity = 20_000
            }
        }
    }

    fun logUserAction(action: String, metadata: Map<String, String> = emptyMap()) {
        SpectraLogger.i("UserAction", action, metadata = metadata)
    }

    fun logError(tag: String, message: String, error: Throwable? = null) {
        SpectraLogger.e(tag, message, error)
    }
}
```

#### 3. Initialize in Platform Code

**Android:**
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.initialize()
    }
}
```

**iOS:**
```swift
@main
struct MyApp: App {
    init() {
        AppLogger.shared.initialize()
    }
}
```

---

## Common Use Cases

### 1. Error Logging with Stack Traces

```kotlin
try {
    fetchDataFromAPI()
} catch (e: Exception) {
    SpectraLogger.e(
        tag = "API",
        message = "Failed to fetch data",
        throwable = e,
        metadata = mapOf(
            "endpoint" to "/api/users",
            "retry_count" to "3"
        )
    )
}
```

### 2. User Analytics

```kotlin
fun trackUserEvent(event: String, properties: Map<String, String> = emptyMap()) {
    SpectraLogger.i(
        tag = "Analytics",
        message = event,
        metadata = properties + mapOf(
            "timestamp" to Clock.System.now().toString(),
            "user_id" to getCurrentUserId()
        )
    )
}

// Usage
trackUserEvent(
    event = "purchase_completed",
    properties = mapOf(
        "item_id" to "12345",
        "amount" to "99.99",
        "currency" to "USD"
    )
)
```

### 3. Performance Monitoring

```kotlin
suspend fun performOperation() {
    val startTime = Clock.System.now()

    try {
        // Your operation
        heavyComputation()

        val duration = Clock.System.now() - startTime
        SpectraLogger.d(
            tag = "Performance",
            message = "Operation completed",
            metadata = mapOf(
                "duration_ms" to duration.inWholeMilliseconds.toString(),
                "operation" to "heavyComputation"
            )
        )
    } catch (e: Exception) {
        SpectraLogger.w(
            tag = "Performance",
            message = "Operation failed",
            throwable = e
        )
    }
}
```

### 4. Debug Logging with Conditional Levels

```kotlin
object DebugLogger {
    private val isDebugBuild = BuildConfig.DEBUG

    fun verbose(tag: String, message: String) {
        if (isDebugBuild) {
            SpectraLogger.v(tag, message)
        }
    }

    fun debug(tag: String, message: String) {
        if (isDebugBuild) {
            SpectraLogger.d(tag, message)
        }
    }
}
```

### 5. Network Request Logging

```kotlin
suspend fun makeApiCall(url: String): Response {
    SpectraLogger.d("Network", "Request started: $url")

    val response = try {
        client.get(url)
    } catch (e: Exception) {
        SpectraLogger.e("Network", "Request failed: $url", e)
        throw e
    }

    SpectraLogger.i(
        tag = "Network",
        message = "Request completed: $url",
        metadata = mapOf(
            "status" to response.status.value.toString(),
            "duration_ms" to response.responseTime.toString()
        )
    )

    return response
}
```

---

## Advanced Features

### Custom Storage Implementation

```kotlin
class CustomLogStorage : LogStorage {
    private val database: Database = createDatabase()

    override suspend fun add(entry: LogEntry) {
        database.insert(entry)
    }

    override suspend fun query(filter: LogFilter, limit: Int?): List<LogEntry> {
        return database.query(filter, limit)
    }

    // Implement other methods...
}

// Use custom storage
SpectraLogger.configure {
    // Custom storage configuration
}
```

### Filtering and Searching

```kotlin
// Filter by multiple criteria
val criticalErrors = SpectraLogger.query(
    filter = LogFilter(
        levels = setOf(LogLevel.ERROR, LogLevel.FATAL),
        tags = setOf("Database", "Network"),
        startTime = Clock.System.now() - 1.hours
    )
)

// Search in messages
val authLogs = SpectraLogger.query(
    filter = LogFilter(messagePattern = "authentication")
)
```

### Export Logs

```kotlin
suspend fun exportLogs(): String {
    val logs = SpectraLogger.query()
    return logs.joinToString("\n") { log ->
        "${log.timestamp} [${log.level}] ${log.tag}: ${log.message}"
    }
}

// Or export as JSON
suspend fun exportLogsAsJson(): String {
    val logs = SpectraLogger.query()
    return Json.encodeToString(logs)
}
```

---

## UI Integration

### Show Debug Screen in Development

```kotlin
@Composable
fun App() {
    val showDebug = remember { BuildConfig.DEBUG }

    Box {
        MainAppContent()

        if (showDebug) {
            FloatingActionButton(
                onClick = { /* Navigate to debug screen */ },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.BugReport, "Debug")
            }
        }
    }
}
```

### Debug Drawer

```kotlin
@Composable
fun AppWithDebugDrawer() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column {
                Text("Debug Tools", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { /* Show logs */ }) {
                    Text("View Logs")
                }
                TextButton(onClick = { /* Show network */ }) {
                    Text("Network Logs")
                }
                TextButton(onClick = { /* Settings */ }) {
                    Text("Logger Settings")
                }
            }
        }
    ) {
        MainContent()
    }
}
```

---

## Best Practices

1. **Initialize early**: Configure logger in Application class (Android) or App init (iOS)
2. **Use appropriate levels**: Don't log everything as INFO or ERROR
3. **Include context**: Use metadata for searchable context
4. **Clean production logs**: Set higher minLogLevel in release builds
5. **Monitor performance**: Use performance logs to track slow operations
6. **Export capability**: Provide users a way to export logs for debugging

---

## Next Steps

- [API Reference](./API.md)
- [Examples](../examples/)
- [Performance Guide](./PERFORMANCE.md)
- [Troubleshooting](./TROUBLESHOOTING.md)
