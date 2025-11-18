# Android Project Structure & Configuration

This is a complete, standalone Android Studio project for the Spectra Logger example app.

## Project Layout

```
android-native/
├── .idea/                          # Android Studio IDE configuration
│   ├── compiler.xml               # Compiler settings (Java 11)
│   ├── gradle.xml                 # Gradle configuration
│   ├── misc.xml                   # Project root manager settings
│   ├── modules.xml                # Module definitions
│   ├── vcs.xml                    # Version control settings
│   ├── workspace.xml              # IDE workspace state
│   └── modules/
│       ├── SpectraLoggerExample.iml      # Root module
│       └── app/
│           └── SpectraLoggerExample.app.iml  # App module
├── gradle/
│   └── wrapper/                   # Gradle wrapper for version 8.13
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/spectra/logger/
│   │   │   │   ├── SpectraLogger.kt       # Mock logger implementation
│   │   │   │   ├── domain/model/
│   │   │   │   │   └── LogLevel.kt        # Log level enum
│   │   │   │   └── example/
│   │   │   │       ├── MainActivity.kt    # Main app UI (Material3 Compose)
│   │   │   │       └── ui/...            # UI components
│   │   │   ├── res/                      # Resources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml       # App manifest
│   │   ├── test/                         # Unit tests
│   │   └── androidTest/                  # Android instrumented tests
│   ├── build.gradle.kts            # App-level build configuration
│   └── detekt-baseline.xml         # Code quality baseline
├── build.gradle.kts               # Root-level build configuration
├── settings.gradle.kts            # Gradle project settings & modules
├── gradle.properties              # Gradle system properties
├── gradlew & gradlew.bat          # Gradle wrapper scripts
├── OPEN_IN_ANDROID_STUDIO.md      # Quick start guide
├── PROJECT_STRUCTURE.md           # This file
└── README.md                      # Project documentation

```

## Gradle Configuration

### settings.gradle.kts
Defines the project structure:
- Project name: `SpectraLoggerExample`
- Includes module: `:app`
- Configures repositories (Google, Maven Central)
- Configures plugin management

### build.gradle.kts (Root)
Defines root-level plugins:
- `android.application` plugin (not applied, for sub-modules)
- `kotlin.android` plugin (not applied, for sub-modules)

### app/build.gradle.kts
Defines app module configuration:
- **Namespace**: `com.spectra.logger.example`
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Java compatibility**: Java 11
- **Compose**: Enabled with version 1.5.8
- **Dependencies**:
  - AndroidX Core, Lifecycle, Activity
  - Jetpack Compose (UI, Material3)
  - Jetpack Navigation Compose
  - Kotlinx DateTime

### gradle.properties
System-wide Gradle settings:
- Kotlin code style: official
- Android X support: enabled
- JVM args: -Xmx2048m (max heap)
- Parallel builds: enabled
- Build cache: enabled

## Code Organization

### com.spectra.logger (Domain)
**SpectraLogger.kt** - Mock logger singleton
- Provides `v()`, `d()`, `i()`, `w()`, `e()`, `f()` logging methods
- Accepts optional metadata parameter
- Minimal implementation for example purposes
- All log calls print to console

**domain/model/LogLevel.kt** - Log level enumeration
- VERBOSE, DEBUG, INFO, WARNING, ERROR, FATAL
- Used for log filtering and categorization

### com.spectra.logger.example (Application)
**MainActivity.kt** - Complete Material3 UI implementation
- BottomNavigationBar with 3 tabs
- Logs tab - displays app logs with search/filter
- Network tab - displays network requests/responses
- Settings tab - app configuration options
- Uses Jetpack Compose for UI
- Implements MVVM pattern with ViewModel

## Android Studio Configuration

### .idea/compiler.xml
Sets bytecode target to Java 11

### .idea/gradle.xml
Configures Gradle integration:
- Test runner: GRADLE
- Distribution type: DEFAULT_WRAPPED (uses wrapper)
- Gradle JVM: corretto-11
- Modules: root and :app

### .idea/misc.xml
Project-level settings:
- JDK name: corretto-11
- JDK type: JavaSDK
- Framewor detection exclusions

### .idea/modules.xml
Lists project modules:
- Root: SpectraLoggerExample
- App: SpectraLoggerExample.app

### .idea/modules/SpectraLoggerExample.iml
Root module configuration:
- Android facet
- Build output directory: build/

### .idea/modules/app/SpectraLoggerExample.app.iml
App module configuration:
- Android Gradle facet with AGP version 8.5.0
- Source folders: src/main/kotlin, src/main/java, res
- Build artifacts and generated sources
- Library dependencies for runtime

### .idea/workspace.xml
IDE workspace state:
- Git integration settings
- UI preferences
- Recent files

## Why This Structure Works

1. **Standalone Project**: No dependency on root Spectra project
2. **Mock Implementations**: SpectraLogger and LogLevel are local, not from :shared
3. **Complete Configuration**: All .idea files present so Android Studio recognizes it immediately
4. **Gradle Wrapper**: Self-contained Gradle version ensures consistency
5. **Standard Layout**: Follows Android project conventions

## Build & Run

### Command Line
```bash
./gradlew build          # Full build
./gradlew assembleDebug  # Debug APK
./gradlew assemble      # All APKs
./gradlew run          # Run on emulator/device
```

### Android Studio
1. File → Open → `/path/to/android-native`
2. Wait for Gradle sync (2-3 minutes first time)
3. Build → Build Project
4. Run → Run 'app' → Select device

## Output

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk` (~8.5 MB)
- **Release APK**: `app/build/outputs/apk/release/app-release.apk` (~5.8 MB)

## Next Steps

1. Open in Android Studio using the 3-step process
2. Build the project
3. Run on emulator or physical device
4. Explore the Material3 UI
5. Modify and extend as needed
