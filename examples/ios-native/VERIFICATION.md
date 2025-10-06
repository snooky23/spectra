# iOS Example App Verification

This document helps verify that all 3 requested features are working correctly.

## ✅ Feature 1: Main App Screen with Button

**Expected Behavior:**
- App launches with a main screen showing "Example App"
- Screen has 3 example action buttons:
  - "Tap Me (Generates Log)" - Blue button
  - "Generate Warning" - Orange button
  - "Generate Error" - Red button
- Bottom has purple "Open Spectra Logger" button

**Verification Steps:**
1. Open `SpectraExample.xcodeproj` in Xcode
2. Select iPhone simulator (iPhone 15 Pro)
3. Press Cmd+R to build and run
4. Main screen should appear with all buttons
5. Tap "Open Spectra Logger" button
6. Spectra Logger UI should appear as a modal sheet
7. Tap "Done" to dismiss and return to main screen

**Expected Result:** ✅ Main app screen with working button to open Spectra Logger

---

## ✅ Feature 2: URL Scheme Support - Opening Logs

**Supported URL Schemes:**
- `spectralogger://logs` - Opens logs screen
- `spectralogger://network` - Opens network logs screen
- `spectralogger://clear` - Clears all logs

**Verification Steps:**

### Method 1: Terminal (with simulator running)
```bash
# Test opening logs screen
xcrun simctl openurl booted spectralogger://logs

# Test opening network logs
xcrun simctl openurl booted spectralogger://network

# Test clear logs
xcrun simctl openurl booted spectralogger://clear
```

### Method 2: Safari on Simulator
1. Open Safari in the simulator
2. Type in address bar: `spectralogger://logs`
3. Press Enter
4. App should open automatically

### Method 3: Check Logs
After triggering any URL scheme, check the app logs:
1. In Xcode, open Debug Console (Cmd+Shift+Y)
2. Look for log entries like:
   ```
   [DeepLink] App opened with URL: spectralogger://logs
   [DeepLink] Opening logs screen
   ```

**Expected Result:** ✅ URL schemes successfully trigger the app and log the action

---

## ✅ Feature 3: URL Scheme Configuration

**Verification Steps:**

### Check Project Settings
1. Open `SpectraExample.xcodeproj` in Xcode
2. Select "SpectraExample" target
3. Go to "Info" tab
4. Look for "URL Types" section (may need to expand)
5. Should see:
   - **Identifier:** com.spectra.logger
   - **URL Schemes:** spectralogger
   - **Role:** Editor

### Check in Build Settings
1. In Xcode, go to Build Settings
2. Search for "URL"
3. Look for these settings:
   - `INFOPLIST_KEY_CFBundleURLTypes[0][CFBundleTypeRole] = Editor`
   - `INFOPLIST_KEY_CFBundleURLTypes[0][CFBundleURLName] = com.spectra.logger`
   - `INFOPLIST_KEY_CFBundleURLTypes[0][CFBundleURLSchemes][0] = spectralogger`

### Verify in Code
Check that `SpectraExampleApp.swift` contains:
```swift
.onOpenURL { url in
    handleURL(url)
}

private func handleURL(_ url: URL) {
    // Handles spectralogger:// URLs
}
```

**Expected Result:** ✅ URL scheme properly configured in Xcode project

---

## Quick Test Script

Run all verification steps at once (requires simulator to be running):

```bash
#!/bin/bash

echo "🧪 Testing iOS Example App Features"
echo ""

echo "1️⃣ Testing URL Scheme: spectralogger://logs"
xcrun simctl openurl booted spectralogger://logs
sleep 2

echo "2️⃣ Testing URL Scheme: spectralogger://network"
xcrun simctl openurl booted spectralogger://network
sleep 2

echo "3️⃣ Testing URL Scheme: spectralogger://clear"
xcrun simctl openurl booted spectralogger://clear
sleep 2

echo ""
echo "✅ All URL schemes tested!"
echo "Check Xcode console for deep link logs"
```

Save this as `test-url-schemes.sh`, make it executable, and run:
```bash
chmod +x test-url-schemes.sh
./test-url-schemes.sh
```

---

## Troubleshooting

### Issue: MainAppView not showing
**Solution:** Make sure you regenerated the Xcode project:
```bash
cd examples/ios-native
./create-xcode-project.sh
```

### Issue: URL schemes not working
**Solution:** Clean build and reinstall app:
1. In Xcode: Product → Clean Build Folder (Cmd+Shift+K)
2. Delete app from simulator
3. Build and run again (Cmd+R)

### Issue: "No such module 'SpectraLogger'"
**Solution:** Build the framework first:
```bash
cd ../..
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### Issue: Can't see deep link logs
**Solution:** Make sure Debug Console is visible:
- In Xcode: View → Debug Area → Show Debug Area (Cmd+Shift+Y)
- Look for logs tagged with [DeepLink]

---

## Expected Console Output

When URL schemes work correctly, you should see logs like:

```
[App] Spectra Logger iOS Example Started
[App] Version: 1.0.0
[DeepLink] App opened with URL: spectralogger://logs
[DeepLink] Opening logs screen
[Navigation] Opening Spectra Logger UI
```

---

## Success Criteria

All 3 features are working if:
- ✅ Main app screen displays with "Open Spectra Logger" button
- ✅ Button opens Spectra Logger UI in a modal sheet
- ✅ URL schemes trigger the app (confirmed by console logs)
- ✅ URL scheme configuration visible in Xcode project settings
