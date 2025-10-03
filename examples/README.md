# Spectra Logger Examples

This directory contains example applications demonstrating how to use the Spectra Logger framework.

## Android Native Example

Located in `android-native/`, this demonstrates using Spectra Logger in a traditional Android native app.

To run:
```bash
./gradlew :examples:android-native:app:installDebug
```

## KMP Example

Located in `kmp-app/`, this demonstrates using Spectra Logger in a Kotlin Multiplatform project.

### Android
```bash
./gradlew :examples:kmp-app:androidApp:installDebug
```

### iOS
Open `examples/kmp-app/iosApp/iosApp.xcodeproj` in Xcode and run.

## Note

These are placeholder examples for Phase 4. Full implementation will include:
- Complete UI screens for log display
- Network request logging
- Filtering capabilities
- Export functionality
