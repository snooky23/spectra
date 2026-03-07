# Configuration Reference

Spectra Logger provides a comprehensive DSL to configure its behavior at runtime. This configuration primarily takes place during the initial setup in your application's `onCreate()` (Android) or `init()` (iOS).

## LoggerConfiguration

The `LoggerConfiguration` object acts as the root configuration node. It controls global logger settings and encapsulates sub-configurations for specific domains.

```kotlin
SpectraLogger.configure {
    // Top-Level Setting: Determines the minimum severity of logs captured.
    minLogLevel = LogLevel.DEBUG

    // Defines application context affixed to every captured log.
    appContext = AppContext(
        sessionId = UUID.randomUUID().toString(),
        appVersion = "1.0.0",
        // ...
    )

    // Configuration blocks for sub-domains:
    logStorage { ... }
    networkStorage { ... }
    performance { ... }
    features { ... }
}
```

### Properties

- `minLogLevel` (`LogLevel`, default: `VERBOSE`): The minimum severity level a log event must possess to be collected. Events below this level are ignored.
- `appContext` (`AppContext`): Metadata automatically injected into all locally captured logs (e.g., App Version, OS, Device Model). Providing this context greatly enriches querying capabilities.

---

## Log Storage Configuration

The `logStorage` and `networkStorage` blocks configure how logs are persisted, leveraging the `StorageConfiguration` data class.

```kotlin
SpectraLogger.configure {
    logStorage {
        maxCapacity = 15_000
        enablePersistence = true
        fileLogLevel = LogLevel.INFO
    }

    networkStorage {
        maxCapacity = 2_000
        enablePersistence = false
        // networkStorage ignores fileLogLevel since all HTTP logs are inherently detailed
    }
}
```

### Properties (`StorageConfiguration`)

- `maxCapacity` (`Int`, default: `10000`): The maximum number of log items tracked in memory. When the limit is reached, Spectra uses a circular buffer to evict the oldest logs. Keep this number reasonable to avoid memory pressure on constrained mobile devices.
- `enablePersistence` (`Boolean`, default: `false`): If `true`, logs are safely persisted to the local file system. This allows crashes and previous sessions to report historical logs robustly.
- `fileLogLevel` (`LogLevel`, default: `INFO`): When `enablePersistence` is true, this controls the minimum log severity that gets committed to disk. For instance, you may want `minLogLevel = VERBOSE` for in-memory debug screens, but `fileLogLevel = WARNING` to keep disk usage low on the user's phone.

---

## Features (FeatureFlags Configuration)

The `features` block manages the activation of major SDK pillars.

```kotlin
SpectraLogger.configure {
    features {
        enableNetworkLogging = true
        networkIgnoredDomains = listOf("analytics.google.com", "metrics.internal.org")
        networkIgnoredExtensions = listOf("png", "jpg", "css", "js")
    }
}
```

### Properties (`FeatureFlags`)

- `enableNetworkLogging` (`Boolean`, default: `true`): Toggles whether network interceptors evaluate requests. Setting this to `false` disables all HTTP traffic tracking entirely.
- `networkIgnoredDomains` (`List<String>`, default: `emptyList()`): A list of host domains that should bypass network logging. Any HTTP URL containing one of these substrings in its host will not be logged. Extremely useful for silencing noisy telemetry or analytics tracking.
- `networkIgnoredExtensions` (`List<String>`, default: `listOf("png", "jpg", "jpeg", "gif", "svg", "ico")`): A list of file extensions that bypass network logging. This prevents massive blobs of binary asset data from clogging the network logger.
- `enableCrashReporting` (`Boolean`, default: `false`): (Planned) Will intercept uncaught exceptions and log them as `FATAL`.
- `enablePerformanceMetrics` (`Boolean`, default: `false`): (Planned) Will attach CPU/Memory footprint snapshots periodically to the log stream.

---

## Performance Configuration

The `performance` block tuning allows overriding the internal mechanisms of Spectra Logger's concurrency models. Use caution when modifying these from their defaults.

```kotlin
SpectraLogger.configure {
    performance {
        flowBufferCapacity = 128
        asyncWriteTimeout = 5000L
        maxBodySize = 10_000
    }
}
```

### Properties (`PerformanceConfiguration`)

- `flowBufferCapacity` (`Int`, default: `64`): Controls the capacity of the `MutableSharedFlow` buffers handling live log streams.
- `asyncWriteTimeout` (`Long`, default: `5000L`): Timeout in milliseconds given to `LogStorage` implementations to suspend during block operations.
- `maxBodySize` (`Int`, default: `10000`): Maximum size in bytes of a Network request/response body that will be captured. Payloads exceeding this limit will be truncated or omitted to save memory.
