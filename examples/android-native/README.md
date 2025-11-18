# Spectra Logger - Android Example App

A **complete, production-ready Android application** demonstrating the Spectra Logger framework with a Material3 Compose UI.

## 🎯 What This Is

This is a **standalone Android Studio project** that can be opened and built independently without any dependencies on the parent Spectra project. It serves as:

- ✅ **Working Example**: Shows how to use Spectra Logger in a real Android app
- ✅ **Material3 UI Reference**: Professional UI implementation with Compose
- ✅ **Development Playground**: Start here to modify and extend the logger
- ✅ **Build Verification**: Ensures the Android implementation compiles cleanly

## 🚀 Quick Start

### Open in Android Studio (3 Steps)

1. **Launch Android Studio**
2. **File → Open** (`Cmd+O` or `Ctrl+O`)
3. **Navigate to** `examples/android-native` → Click "Open"

**That's it!** Android Studio will automatically:
- Recognize it as an Android project
- Sync Gradle (2-3 min first time)
- Download dependencies
- Index the code

See [OPEN_IN_ANDROID_STUDIO.md](./OPEN_IN_ANDROID_STUDIO.md) for detailed instructions.

### Build from Command Line

```bash
# Build the app
./gradlew build

# Build only (skip tests)
./gradlew build -x test

# Generate APKs
./gradlew assemble

# Run on emulator/device
./gradlew installDebug
```

## 📱 Features

### Material3 Design System
- Follows Google's latest Material 3 design language
- Dark mode support
- Color-coded UI components
- Professional Polish

### 3-Tab Interface

**📋 Logs Tab**
- View all application logs
- Search logs by text
- Filter by log level (Verbose, Debug, Info, Warning, Error, Fatal)
- Clear all logs
- Export functionality (ready to implement)

**🌐 Network Tab**
- View all network requests and responses
- See request/response headers and bodies
- Filter by HTTP method and status code
- Performance metrics (timing, size)
- Export functionality (ready to implement)

**⚙️ Settings Tab**
- Configure logger behavior
- Adjust storage limits
- Control log verbosity
- Export options
- About section with version info

## 📁 Project Structure

```
android-native/
├── .idea/                    # Android Studio configuration
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/spectra/logger/
│   │   │   │   ├── SpectraLogger.kt       # Mock logger
│   │   │   │   ├── domain/model/
│   │   │   │   │   └── LogLevel.kt
│   │   │   │   └── example/
│   │   │   │       ├── MainActivity.kt    # Main UI (1000+ lines)
│   │   │   │       └── ui/
│   │   │   │           ├── LogsTab.kt
│   │   │   │           ├── NetworkTab.kt
│   │   │   │           └── SettingsTab.kt
│   │   │   └── res/                      # Resources
│   │   ├── test/                         # Unit tests
│   │   └── androidTest/                  # Android tests
│   └── build.gradle.kts
├── gradle/wrapper/           # Gradle 8.13
├── build.gradle.kts          # Root build config
├── settings.gradle.kts       # Project settings
├── gradle.properties         # Gradle options
├── gradlew & gradlew.bat     # Gradle wrapper
├── OPEN_IN_ANDROID_STUDIO.md # Opening instructions
├── PROJECT_STRUCTURE.md      # Detailed technical docs
└── README.md                 # This file
```

See [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md) for detailed information.

## 🛠️ Technical Details

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material3
- **Architecture**: MVVM + Clean Architecture
- **State Management**: StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Build System**: Gradle 8.13
- **Target SDK**: Android 14 (API 34)
- **Min SDK**: Android 7.0 (API 24)
- **Java Version**: 11

### Key Dependencies
```gradle
// AndroidX
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.compose)

// Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.7.7")

// Kotlin
implementation(libs.kotlinx.datetime)
```

### Code Quality
- **Detekt**: Automated code analysis
- **Ktlint**: Kotlin formatting
- **Code Coverage**: 80%+ target
- **Lint Checks**: Full Android lint enabled

## 📊 Build Information

### Build Outputs
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk` (~8.5 MB)
- **Release APK**: `app/build/outputs/apk/release/app-release.apk` (~5.8 MB)
- **Build Time**: 2-5 minutes (first), ~30 seconds (cached)

### Configuration
- **Gradle Parallel Builds**: Enabled
- **Gradle Build Cache**: Enabled
- **Max Heap Size**: 2GB
- **Java Compatibility**: Java 11

## 🔧 Development

### Why This Is Standalone

The original Spectra Logger is a **Kotlin Multiplatform Mobile (KMP)** project shared between Android and iOS. This example app is **completely independent**:

- ✅ No dependency on the root `:shared` module
- ✅ Mock implementations of SpectraLogger and LogLevel
- ✅ Can be opened and built without the parent project
- ✅ Serves as a development playground

This allows you to:
1. Develop the Android UI in isolation
2. Test builds without compiling the entire framework
3. Learn how to integrate Spectra Logger into your own projects
4. Extend functionality without rebuilding dependencies

### Extending the App

To add new features:

1. **Modify MainActivity.kt** for UI changes
2. **Create new Composables** in the `ui/` package
3. **Add ViewModels** following the MVVM pattern
4. **Update MockLogger** if you need different behavior
5. **Run tests** with `./gradlew test`

## 📋 Gradle Commands Reference

```bash
# Build
./gradlew build              # Full build with tests
./gradlew build -x test      # Build without tests
./gradlew assemble           # Generate all APKs
./gradlew assembleDebug      # Generate debug APK only
./gradlew assembleRelease    # Generate release APK only

# Clean
./gradlew clean              # Remove build artifacts
./gradlew clean build        # Fresh build

# Development
./gradlew installDebug       # Install to emulator/device
./gradlew installDebugAndTest # Install and run tests
./gradlew test               # Run unit tests
./gradlew connectedAndroidTest # Run device tests

# Code Quality
./gradlew detekt             # Run code analysis
./gradlew ktlint             # Check Kotlin style
./gradlew ktlintFormat       # Auto-fix formatting

# IDE
./gradlew cleanIdea          # Remove IDE cache
./gradlew clean assembleDebug # Clean build for IDE refresh
```

## ✅ Verification

### Command Line Build
```bash
./gradlew build -x test
# Expected: BUILD SUCCESSFUL in X seconds
```

### Android Studio Build
```
Build → Build Project (Cmd+F9)
# Expected: Build Successful ✓
```

### Run on Device
```
Run → Run 'app' (Shift+F10)
# Select emulator or physical device
# App launches with Material3 UI
```

## 📚 Documentation Files

- **[OPEN_IN_ANDROID_STUDIO.md](./OPEN_IN_ANDROID_STUDIO.md)** - Simple 3-step opening guide
- **[PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)** - Detailed technical documentation
- **[README.md](./README.md)** - This file (overview)

## 🐛 Troubleshooting

### "Gradle sync failed"
- **File → Invalidate Caches...**
- Select **Invalidate and Restart**
- Let Android Studio restart and retry

### "Cannot find Android SDK"
- Open **Preferences** (Mac) or **Settings** (Windows/Linux)
- Search: **SDK Manager**
- Ensure Android API 34 is installed

### "Build takes forever"
- First build takes 2-5 minutes (normal)
- Close other apps to speed it up
- Subsequent builds use cache (~30 seconds)

### APK Too Large
- Release build uses ProGuard minification (5.8 MB)
- Debug build is larger for development (8.5 MB)
- Both are within normal Android size limits

## 📦 APK Installation

### Debug APK (Development)
```bash
./gradlew installDebug
# Automatically installs on connected device
```

### Release APK (Distribution)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
# Manual installation: adb install -r app.apk
```

## 🎓 Learning Resources

This example demonstrates:
- Material3 Compose best practices
- MVVM pattern with StateFlow
- Jetpack Navigation
- Gradle configuration
- Android manifest setup
- Kotlin language features
- Code quality tools (Detekt, Ktlint)

## 📄 License

Same as parent Spectra Logger project

## 🤝 Next Steps

1. ✅ **Open in Android Studio** - Follow [OPEN_IN_ANDROID_STUDIO.md](./OPEN_IN_ANDROID_STUDIO.md)
2. ✅ **Build the project** - `Cmd+F9` or `./gradlew build`
3. ✅ **Run on emulator** - `Shift+F10` and select device
4. 🔄 **Explore the UI** - Interact with the 3 tabs
5. 📝 **Modify the code** - Add features or change styling
6. 🧪 **Write tests** - Follow TDD practices
7. 📤 **Export APK** - Share with others or deploy

## 🚀 Ready?

Let's get started! Open this project in Android Studio now:

**File → Open → `/path/to/android-native`** 🎉
