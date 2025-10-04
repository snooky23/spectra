# Spectra Logger API Reference

Complete API documentation for Spectra Logger.

## Table of Contents

- [Core API](#core-api)
- [Configuration](#configuration)
- [Log Levels](#log-levels)
- [Storage](#storage)
- [Network Logging](#network-logging)
- [UI Components](#ui-components)
- [Platform Integration](#platform-integration)

---

## Core API

### SpectraLogger

The main entry point for logging operations.

```kotlin
object SpectraLogger {
    // Logging methods
    fun v(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, String> = emptyMap())
    fun d(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, String> = emptyMap())
    fun i(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, String> = emptyMap())
    fun w(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, String> = emptyMap())
    fun e(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, String> = emptyMap())
    fun f(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, String> = emptyMap())

    // Query methods
    suspend fun query(filter: LogFilter = LogFilter.NONE, limit: Int? = null): List<LogEntry>
    suspend fun count(): Int
    suspend fun clear()

    // Network logging
    suspend fun queryNetwork(filter: NetworkLogFilter = NetworkLogFilter.NONE): List<NetworkLogEntry>
    suspend fun networkCount(): Int
    suspend fun clearNetwork()

    // Configuration
    fun configure(block: LoggerConfigurationBuilder.() -> Unit)
    val configuration: LoggerConfiguration

    // Storage access
    val logStorage: LogStorage
    val networkStorage: NetworkLogStorage

    // Version
    fun getVersion(): String
}
```

### Usage Examples

#### Basic Logging

```kotlin
// Verbose
SpectraLogger.v("UI", "Button clicked")

// Debug
SpectraLogger.d("Network", "API request sent")

// Info
SpectraLogger.i("Auth", "User logged in")

// Warning
SpectraLogger.w("Performance", "Slow operation detected")

// Error
SpectraLogger.e("Database", "Failed to save data")

// Fatal
SpectraLogger.f("Critical", "App state corrupted")
```

#### Logging with Throwable

```kotlin
try {
    performRiskyOperation()
} catch (e: Exception) {
    SpectraLogger.e("App", "Operation failed", e)
}
```

#### Logging with Metadata

```kotlin
SpectraLogger.i(
    tag = "Analytics",
    message = "User action",
    metadata = mapOf(
        "user_id" to "12345",
        "action" to "purchase",
        "amount" to "99.99"
    )
)
```

---

## Configuration

### LoggerConfiguration

Configure logger behavior using the DSL:

```kotlin
SpectraLogger.configure {
    minLogLevel = LogLevel.DEBUG

    logStorage {
        maxCapacity = 20_000
        enablePersistence = true
    }

    networkStorage {
        maxCapacity = 2_000
    }

    performance {
        flowBufferCapacity = 128
        asyncWriteTimeout = 5_000
        maxBodySize = 10_000
    }

    features {
        enableNetworkLogging = true
        enableCrashReporting = false
        enablePerformanceMetrics = false
    }
}
```

### Configuration Options

#### Storage Configuration
- `maxCapacity: Int` - Maximum number of logs to store (default: 10,000)
- `enablePersistence: Boolean` - Enable file-based persistence (default: false)

#### Performance Configuration
- `flowBufferCapacity: Int` - Flow buffer size (default: 64)
- `asyncWriteTimeout: Long` - Write timeout in ms (default: 5,000)
- `maxBodySize: Int` - Max network body size in bytes (default: 10,000)

#### Feature Flags
- `enableNetworkLogging: Boolean` - Enable network interception (default: true)
- `enableCrashReporting: Boolean` - Enable crash reporting (default: false)
- `enablePerformanceMetrics: Boolean` - Enable perf metrics (default: false)

---

## Log Levels

```kotlin
enum class LogLevel(val priority: Int) {
    VERBOSE(0),
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    FATAL(5)
}
```

Logs are filtered based on `minLogLevel`. Only logs with equal or higher priority are captured.

---

## Storage

### LogStorage Interface

```kotlin
interface LogStorage {
    suspend fun add(entry: LogEntry)
    suspend fun addAll(entries: List<LogEntry>)
    suspend fun query(filter: LogFilter = LogFilter.NONE, limit: Int? = null): List<LogEntry>
    fun observe(filter: LogFilter = LogFilter.NONE): Flow<LogEntry>
    suspend fun count(): Int
    suspend fun clear()
}
```

### LogFilter

```kotlin
data class LogFilter(
    val levels: Set<LogLevel>? = null,
    val tags: Set<String>? = null,
    val messagePattern: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val metadataKey: String? = null
)
```

### Example: Filtering Logs

```kotlin
// Filter by level
val errors = SpectraLogger.query(
    filter = LogFilter(levels = setOf(LogLevel.ERROR, LogLevel.FATAL))
)

// Filter by tag
val networkLogs = SpectraLogger.query(
    filter = LogFilter(tags = setOf("Network", "API"))
)

// Filter by message pattern
val authLogs = SpectraLogger.query(
    filter = LogFilter(messagePattern = "auth")
)
```

---

## Network Logging

### NetworkLogEntry

```kotlin
data class NetworkLogEntry(
    val id: String,
    val timestamp: Instant,
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseCode: Int? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val duration: Long = 0,
    val error: String? = null
)
```

### Network Query

```kotlin
// Get all network logs
val networkLogs = SpectraLogger.queryNetwork()

// Filter by method
val postRequests = SpectraLogger.queryNetwork(
    filter = NetworkLogFilter(methods = setOf("POST", "PUT"))
)

// Filter by status code
val errors = SpectraLogger.queryNetwork(
    filter = NetworkLogFilter(statusCodes = setOf(404, 500, 503))
)
```

---

## UI Components

### LogListScreen

Display application logs with filtering and search.

```kotlin
@Composable
fun LogListScreen(
    storage: LogStorage,
    filter: LogFilter = LogFilter.NONE,
    modifier: Modifier = Modifier
)
```

### NetworkLogScreen

Display network logs with method and status filtering.

```kotlin
@Composable
fun NetworkLogScreen(
    storage: NetworkLogStorage,
    filter: NetworkLogFilter = NetworkLogFilter.NONE,
    modifier: Modifier = Modifier
)
```

### SettingsScreen

Configure logger settings at runtime.

```kotlin
@Composable
fun SettingsScreen(
    logStorage: LogStorage,
    networkLogStorage: NetworkLogStorage,
    currentMinLevel: LogLevel = LogLevel.VERBOSE,
    onMinLevelChange: (LogLevel) -> Unit = {},
    onExportLogs: () -> Unit = {},
    modifier: Modifier = Modifier
)
```

---

## Platform Integration

### Android

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
        }
    }
}
```

### iOS (Swift)

```swift
import shared

@main
struct MyApp: App {
    init() {
        SpectraLogger.shared.configure { config in
            config.minLogLevel = LogLevel.verbose
            config.logStorage { storage in
                storage.maxCapacity = 20000
                storage.enablePersistence = true
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### Compose Multiplatform

```kotlin
@Composable
fun App() {
    MaterialTheme {
        var selectedScreen by remember { mutableStateOf("logs") }

        when (selectedScreen) {
            "logs" -> LogListScreen(storage = SpectraLogger.logStorage)
            "network" -> NetworkLogScreen(storage = SpectraLogger.networkStorage)
            "settings" -> SettingsScreen(
                logStorage = SpectraLogger.logStorage,
                networkLogStorage = SpectraLogger.networkStorage,
                currentMinLevel = SpectraLogger.configuration.minLogLevel,
                onMinLevelChange = { level ->
                    SpectraLogger.configure { minLogLevel = level }
                }
            )
        }
    }
}
```

---

## Performance Characteristics

| Operation | Target | Typical |
|-----------|--------|---------|
| Log capture | < 0.1ms | ~50μs |
| Network log | < 5ms | ~2ms |
| Storage write | < 1ms | ~500μs |
| Query (10K logs) | < 10ms | ~5ms |
| UI scroll (60 FPS) | 16.67ms/frame | ~8ms |
| Memory (10K logs) | < 50MB | ~30MB |

---

## Best Practices

1. **Use appropriate log levels**
   - VERBOSE: Detailed diagnostic information
   - DEBUG: Development-time debugging
   - INFO: General informational messages
   - WARNING: Potentially harmful situations
   - ERROR: Error events
   - FATAL: Critical errors

2. **Include context in metadata**
   ```kotlin
   SpectraLogger.i(
       "User",
       "Profile updated",
       metadata = mapOf(
           "user_id" to userId,
           "screen" to "profile",
           "fields_changed" to "email,phone"
       )
   )
   ```

3. **Configure for production**
   ```kotlin
   SpectraLogger.configure {
       minLogLevel = if (isDebug) LogLevel.VERBOSE else LogLevel.WARNING
       logStorage {
           maxCapacity = if (isDebug) 50_000 else 10_000
           enablePersistence = !isDebug
       }
   }
   ```

4. **Clean up old logs**
   ```kotlin
   // Clear logs older than 7 days
   SpectraLogger.configure {
       logStorage {
           enablePersistence = true
           // Automatic rotation after 7 days
       }
   }
   ```

---

## Troubleshooting

### Logs not appearing
- Check `minLogLevel` configuration
- Verify log level priority (VERBOSE < DEBUG < INFO < WARNING < ERROR < FATAL)
- Ensure storage capacity not exceeded

### Performance issues
- Reduce `maxCapacity` if memory constrained
- Disable persistence in performance-critical scenarios
- Use filtering to limit query results

### Network logs missing
- Ensure `enableNetworkLogging = true`
- Verify network interceptor is installed
- Check network storage capacity

---

## Support

- **Documentation**: [README.md](../README.md)
- **Examples**: [examples/](../examples/)
- **Issues**: [GitHub Issues](https://github.com/yourusername/spectra-logger/issues)
