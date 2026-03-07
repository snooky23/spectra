# Troubleshooting Guide

This guide addresses common issues encountered when integrating, configuring, or using Spectra Logger in Android, iOS, or Kotlin Multiplatform projects. If you encounter an issue not covered here, please [open an issue](https://github.com/snooky23/spectra/issues) on GitHub.

## 1. Installation Issues

### **Issue:** `Unresolved reference: spectra` (Android / KMP)
- **Cause:** The project cannot find the Spectra artifact.
- **Solution:** 
    1. Ensure `mavenCentral()` is declared in your root `build.gradle.kts` `repositories` block.
    2. Check that the provided dependency version matches the latest release.
    3. Run `Clean Project` and then `Rebuild Project` to force Gradle sync.

### **Issue:** `Missing Required Architecture (arm64)` (iOS CocoaPods)
- **Cause:** Xcode sometimes throws module format errors when mixing SwiftUI architectures via older CocoaPods scripts.
- **Solution:** Under your App target's **Build Settings**, set `Build Active Architecture Only` to `YES` for Debug. Ensure your Podfile has the `post_install` hook shown in the [Installation Guide](./INSTALLATION.md) clamping the iOS Target to `15.0`.

---

## 2. Configuration Issues

### **Issue:** Logging works for Android, but not for iOS (or vice versa).
- **Cause:** You might have configured the builder inside `androidMain` instead of `commonMain`.
- **Solution:** Move the `SpectraLogger.configure { ... }` block to a shared application bootstrapper (e.g., inside `App.kt` or an `init()` method deployed from common code natively).

### **Issue:** Cannot compile `SpectraLogger.configure { networkStorage { ... } }`
- **Cause:** The builder DSL evolves across updates.
- **Solution:** Double-check the exact spelling against the latest [Configuration Reference](./CONFIGURATION.md) and ensure you have updated the SDK. A project sync helps IntelliJ populate the DSL autocomplete properly.

---

## 3. Network Interceptor Issues

### **Issue:** API requests are not showing up in the Network Logs screen.
- **Cause 1:** `enableNetworkLogging = false` is set in the configuration.
    - **Solution:** In your configuration block, explicitly set `enableNetworkLogging = true` under `features {}`.
- **Cause 2:** The URL is blacklisted by default or by your config.
    - **Solution:** Check the array `features.networkIgnoredDomains` or `features.networkIgnoredExtensions`. If your API URL matches those properties, Spectra intentionally skips it. Remove it from the config array.
- **Cause 3:** Interceptor is not attached to `OkHttpClient` (Android) or `URLSession` (iOS).
    - **Solution:** Follow the [Installation Guide](./INSTALLATION.md) closely. For Android, `addInterceptor(SpectraLogger.createOkHttpInterceptor())`. For iOS natively using native URLSession, ensure you invoke `SpectraLogger.registerURLProtocol()`.

### **Issue:** Network payload bodies show `[Truncated or Missing]`.
- **Cause:** The payload body size exceeded the `maxBodySize` defined in the performance configurations, or the Content-Type is opaque/binary.
- **Solution:** Spectra skips huge bodies (like Images, Videos, massive text blobs) to preserve heap memory. Adjust `performance.maxBodySize` upwards cautiously, or verify the endpoint is indeed returning text/JSON.

---

## 4. UI Rendering Issues

### **Issue:** Tapping the FAB doesn't open the logger.
- **Cause:** `SpectraLoggerFabOverlay` relies on `SpectraUIManager.showScreen()` which may fail if the Compose context isn't globally provisioned.
- **Solution:** Ensure your app completely wraps the active hierarchy: `setContent { SpectraLoggerFabOverlay { YourApp() } }`. 

### **Issue:** Physical Shake gesture on iOS isn't triggering the overlay.
- **Cause:** Simulator limitation or incorrect `WindowGroup` attachment.
- **Solution:** On iOS sim, use `Cmd + Control + Z` to simulate a hardware shake. The `.onShakeToRevealSpectraLogger()` view modifier relies on the native `UIWindow` intercepting physics events; ensure it's chained near the top of your SwiftUI `Scene`.

### **Issue:** Dark Mode text is unreadable.
- **Cause:** You are mixing UI kit elements.
- **Solution:** `SpectraLoggerUI` relies on Material 3. Ensure your wrapper doesn't force a localized theme override containing an incomplete color palette that bleeds into the UI overlay.

---

## 5. Storage and Performance

### **Issue:** OutOfMemory (OOME) crashes when logging huge operations.
- **Cause:** `maxCapacity` is set excessively high on constrained memory devices. Keeping 100,000 highly contextualized strings in RAM is expensive.
- **Solution:** Lower the `logStorage.maxCapacity` setting (default is 10,000) and rely instead on File Persistence off-loading.

### **Issue:** Application logs won't persist across fresh launches.
- **Cause:** File persistence is purely opt-in to respect user data footprints.
- **Solution:** Update configuration to `logStorage { enablePersistence = true }`.

### **Issue:** UI stutters when logging heavily inside a `for` loop.
- **Cause:** Pushing thousands of strings per second directly to the `Flow` while the UI observes it forces unnecessary recompositions.
- **Solution:** Ensure your app logic handles giant array operations efficiently. The internal logger is non-blocking, but Compose UI lists lag if their observed flows churn intensely. Wrap heavy bulk transactions in singular `SpectraLogger.i("Bulk Data processed (N=9400)")`.

---

If your issue persists, try resetting emulator state or uninstalling/reinstalling the app. You can also view internal diagnostic logs produced by the SDK by attaching the IDE debugger.
