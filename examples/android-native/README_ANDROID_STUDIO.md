# Spectra Logger Android Example - Android Studio Setup

## Quick Start

### ⭐ Recommended: Open the Root Project

The Android example is part of a multimodule Kotlin Multiplatform project. This is the **only way** to open it in Android Studio because it depends on the `:shared` module.

1. **Open Android Studio**
2. **File → Open** (or Cmd+O)
3. Navigate to the root directory: `/Users/avilevin/Workspace/Personal/Spectra`
4. Click **Open as Project**
5. Android Studio will automatically:
   - Recognize it as a multimodule KMP project
   - Load the Gradle configuration
   - Sync all dependencies (takes 2-3 minutes the first time)
6. In the **Project View** on the left:
   - Expand `examples > android-native > app`
   - Right-click on `app` and select "Set as Active Module" (optional)
7. **Build → Build Project** (or Cmd+F9)
   - First build takes longer (~2-5 minutes)
   - Subsequent builds are faster (~30 seconds)
8. To run the app:
   - **Run → Run 'app'** (or Shift+F10)
   - Select an emulator or connected device
   - The app will launch with the Material3 UI

## Project Structure

```
examples/
└── android-native/
    ├── app/                          # Android application module
    │   ├── src/
    │   │   ├── main/
    │   │   │   ├── java/com/spectra/logger/example/
    │   │   │   │   └── MainActivity.kt  # Complete Compose UI (1045 lines)
    │   │   │   └── AndroidManifest.xml
    │   │   ├── debug/
    │   │   └── test/
    │   ├── build.gradle.kts          # App module configuration
    │   └── proguard-rules.pro
    │
    ├── build.gradle.kts              # Root build configuration
    ├── settings.gradle.kts           # Project structure definition
    ├── gradle.properties             # Gradle configuration
    ├── gradlew                       # Gradle wrapper (Unix)
    └── gradle/                       # Gradle wrapper files & version catalog
```

## What's Inside

The `MainActivity.kt` contains a complete Material3 Compose UI with:

- **Tab Navigation**: Bottom navigation bar with 3 tabs
  - Logs: Searchable list of application logs with filtering
  - Network: Network requests viewer with method badges
  - Settings: Configuration options and storage management

- **Features**:
  - Real-time log display with color-coded severity levels
  - Search bar with clear button
  - Filter chips for log levels and HTTP status codes
  - Network request details with timing information
  - Settings toggle for dark mode
  - Storage usage indicator with progress bar
  - Material3 design system throughout

## Build Commands

From the command line (from the root Spectra project folder):

```bash
# Build the entire project
./gradlew build

# Build only the Android example app
./gradlew examples:android-native:app:build

# Build debug APK
./gradlew examples:android-native:app:assembleDebug

# Build release APK
./gradlew examples:android-native:app:assembleRelease

# Run code quality checks
./gradlew examples:android-native:app:detekt
./gradlew examples:android-native:app:ktlintCheck

# Run tests
./gradlew examples:android-native:app:test
```

## Troubleshooting

### "Cannot resolve symbol 'SpectraLogger'" in Android Studio
This happens when the Gradle sync fails or caches are corrupted.

**Solution**:
1. **File → Invalidate Caches...**
2. Select **Invalidate and Restart**
3. Let Android Studio restart (may take 2-3 minutes)
4. Try building again

### Build fails with "Module not found: :shared"
Only happens if you somehow try to open `android-native` as a standalone project.

**Solution**: Always open the root project at `/Users/avilevin/Workspace/Personal/Spectra`

### Build cache issues
If you get errors after opening in Android Studio:

1. **File → Invalidate Caches...**
2. Select **Invalidate and Restart**
3. Let Android Studio restart and re-index
4. Try building again

### Gradle sync fails
1. Close Android Studio
2. Delete: `android-native/.gradle` directory
3. Reopen and sync

## Generated Files

After building, you'll find:

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`
- **Build reports**: `app/build/reports/`
  - `detekt/` - Code quality analysis
  - `ktlint/` - Code formatting checks

## File Locations

The main UI implementation:
- `examples/android-native/app/src/main/java/com/spectra/logger/example/MainActivity.kt`

Configuration files:
- `examples/android-native/app/build.gradle.kts`
- `examples/android-native/app/src/main/AndroidManifest.xml`

Code quality:
- `examples/android-native/app/detekt-baseline.xml` - Accepted baseline violations

## Need Help?

If Android Studio won't open the project:

1. Make sure you're using **Android Studio Koala or newer**
2. Have **JDK 11+** installed
3. Have at least **8GB RAM** available for Gradle builds
4. Check that the file `/Users/avilevin/Workspace/Personal/Spectra/gradle/wrapper/gradle-wrapper.properties` exists

## Notes

- The example app uses the shared Spectra Logger module for logging functionality
- All UI is built with Jetpack Compose and Material3
- Minimum Android SDK: API 24 (Android 7.0)
- Target Android SDK: API 34 (Android 14)
- Kotlin: 1.9.20+
