# Opening android-native in Android Studio

This guide explains how to properly open the `android-native` project in Android Studio.

## Quick Start

1. **Open Android Studio**
2. **Select "Open" from the Welcome screen** (or File → Open)
3. **Navigate to:** `/Users/avilevin/Workspace/Personal/Spectra/examples/android-native`
4. **Click "Open"**
5. **Wait for Gradle sync to complete** (you should see "Gradle sync finished" at the bottom)

## If You Get an Error

If Android Studio shows an error like "Project SDK is not defined", follow these steps:

### Step 1: Set Project SDK
1. Go to **File → Project Structure**
2. In the left sidebar, select **Project**
3. Set **SDK location** to: `/Users/avilevin/Library/Android/sdk`
4. Click **OK**

### Step 2: Sync Gradle
1. Click **File → Sync Now** (or press Ctrl+Alt+Y / Cmd+Shift+Y)
2. Wait for the sync to complete

### Step 3: Rebuild
1. Click **Build → Rebuild Project**
2. Wait for the build to complete

## Verifying Setup

Once the project is open:

- ✅ You should see the project structure in the left panel:
  ```
  android-native (project root)
  ├── app (module)
  │   ├── manifests
  │   ├── java
  │   ├── res
  │   └── build.gradle.kts
  └── gradle (folder)
  ```

- ✅ The **Build** menu should show no errors
- ✅ You can run the app on an emulator or device

## Running the App

### Option 1: From Android Studio
1. Open an Android Emulator or connect a device
2. Click **Run → Run 'app'** (or press Ctrl+R / Cmd+R)
3. Select your target device/emulator
4. Wait for the app to build and run

### Option 2: From Command Line
```bash
cd /Users/avilevin/Workspace/Personal/Spectra/examples/android-native

# Build the app
./gradlew build

# Install on emulator/device
./gradlew installDebug

# Launch the app
adb shell am start -n com.spectra.logger.example/.MainActivity
```

## Project Details

- **Namespace:** `com.spectra.logger.example`
- **App ID:** `com.spectra.logger.example`
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Kotlin:** 1.9.22
- **Compose:** 2024.02.00
- **Material:** Material 3

## Troubleshooting

### "Gradle sync failed"
- Make sure `local.properties` exists with correct SDK path
- Try File → Invalidate Caches → Invalidate and Restart
- Delete `.gradle` folder and retry

### "Could not resolve dependency"
- Make sure Spectra Logger is built: `cd ../../.. && ./gradlew shared:publishToMavenLocal`
- Clear Gradle cache: `rm -rf ~/.gradle/caches`

### App won't launch
- Make sure emulator is running: `adb devices`
- Check logs: `adb logcat | grep "spectra.logger.example"`

## Files Overview

| File | Purpose |
|------|---------|
| `settings.gradle.kts` | Gradle settings and repositories |
| `build.gradle.kts` | Root build configuration |
| `app/build.gradle.kts` | App module build configuration |
| `app/src/main/AndroidManifest.xml` | Android app manifest |
| `app/src/main/java/...` | Kotlin source files |
| `gradle/libs.versions.toml` | Dependency versions |
| `gradlew` | Gradle wrapper (use `./gradlew` to build) |
| `local.properties` | Local SDK configuration (auto-generated) |

## Next Steps

Once the project is open:

1. **Explore the code:**
   - `MainActivity.kt` - Entry point
   - `MainAppScreen.kt` - UI components and tabs

2. **Run on emulator** to see the app in action

3. **Make changes** and rebuild

## Support

For issues with the Spectra Logger itself, see the main [README](../../README.md).
