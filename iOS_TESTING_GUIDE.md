# iOS Testing Guide - Quick Reference

This document helps you find testing information for different parts of the Spectra Logger iOS integration.

## 📍 Where to Find What

| Need | Location | Purpose |
|------|----------|---------|
| **Testing the iOS Example App** | `examples/ios-native/README.md` | Main guide for running and testing the complete example app with CocoaPods integration |
| **SpectraLoggerUI Swift Package Tests** | `SpectraLoggerUI/README.md` | How to test the pure SwiftUI components |
| **SpectraLoggerUI Installation** | `SpectraLoggerUI/README.md` | How to add SpectraLoggerUI to your own iOS app |
| **In-Depth Testing Guide** | `SpectraLoggerUI/TESTING.md` | Comprehensive guide covering all testing approaches |
| **Kotlin Core Tests** | Run `./gradlew test` | Test the shared KMP module (validates iOS too) |

---

## 🚀 Quick Start (Choose One)

### Option A: Test the Full iOS Example App (Recommended)

Perfect for seeing everything working end-to-end:

```bash
cd examples/ios-native
./setup-pods.sh
open SpectraExample.xcworkspace
# Press Cmd+R to build and run
```

📖 **Full guide**: `examples/ios-native/README.md` → Testing section

---

### Option B: Test Just the SwiftUI Components

If you only want to test the UI package itself:

```bash
cd SpectraLoggerUI
swift test
```

📖 **Full guide**: `SpectraLoggerUI/README.md` → Testing section

---

### Option C: Test the Kotlin Core

Validate iOS functionality from the Kotlin side:

```bash
./gradlew test        # All tests
./gradlew iosTest     # iOS only
./gradlew koverHtmlReport  # Coverage report
```

📖 **Full guide**: `SpectraLoggerUI/TESTING.md` → Part 3: Running Core Kotlin Tests

---

## 📋 Testing Checklist

Use this checklist when testing manually:

```
iOS Example App (examples/ios-native/README.md - Testing section)

App Logs Tab:
  ☐ Tap "Tap Me" button
  ☐ Tap "Generate Warning"
  ☐ Tap "Generate Error"
  ☐ Logs appear in real-time
  ☐ Tap log to view details
  ☐ Search works
  ☐ Filters work

Network Logs Tab:
  ☐ Network requests appear (if integrated)
  ☐ Filter by HTTP method
  ☐ Filter by status code
  ☐ View request/response details
  ☐ Copy cURL command

Settings Tab:
  ☐ Toggle appearance mode (Light/Dark/System)
  ☐ View log counts
  ☐ Clear logs works
  ☐ Export logs works
```

---

## 🛠️ Common Tasks

### Fresh Setup
```bash
cd examples/ios-native
./setup-pods.sh
open SpectraExample.xcworkspace
```

### Update Framework After Code Changes
```bash
cd examples/ios-native
pod update SpectraLogger
```

### Build from Command Line
```bash
xcodebuild -workspace examples/ios-native/SpectraExample.xcworkspace \
           -scheme SpectraExample \
           -configuration Debug \
           -sdk iphonesimulator
```

### Test on Physical Device
Build the framework for arm64 (device):
```bash
./gradlew shared:linkDebugFrameworkIosArm64
cd examples/ios-native
pod install
# Then in Xcode, select a physical device and run
```

### Run Deep Link Test
```bash
xcrun simctl openurl booted spectralogger://logs
```

---

## 🔍 Troubleshooting

| Problem | Solution | Details |
|---------|----------|---------|
| "Module Not Found" | Use `.xcworkspace` not `.xcodeproj` | CocoaPods requires workspace |
| Framework Not Found | `pod install` | Rebuilds the KMP framework |
| Build Fails | Clean everything: `rm -rf Pods Podfile.lock` → `./setup-pods.sh` | See `examples/ios-native/README.md` - Troubleshooting |
| Simulator Crashes | `xcrun simctl shutdown all` → `xcrun simctl erase all` | Reset simulator |

Full troubleshooting: `examples/ios-native/README.md` → Troubleshooting section

---

## 📚 Documentation Files

```
Spectra/
├── examples/ios-native/
│   └── README.md                    ← 🔥 START HERE for example app testing
├── SpectraLoggerUI/
│   ├── README.md                    ← Installation + testing overview
│   └── TESTING.md                   ← Deep-dive testing guide
├── iOS_TESTING_GUIDE.md             ← This file (quick reference)
└── CLAUDE.md                        ← General project instructions
```

---

## ✅ What's Set Up

| Component | Status | Test Command |
|-----------|--------|--------------|
| KMP Core Framework | ✅ Built & Ready | `./gradlew test` |
| SpectraLoggerUI Package | ✅ Ready | `cd SpectraLoggerUI && swift test` |
| iOS Example App | ✅ Configured | `cd examples/ios-native && ./setup-pods.sh && open SpectraExample.xcworkspace` |
| CocoaPods Integration | ✅ Automatic | Runs via `./setup-pods.sh` |

---

## 🎓 Learning Path

1. **First Time?** Read `examples/ios-native/README.md` → "Setup" section
2. **Want to test?** Read `examples/ios-native/README.md` → "Testing" section
3. **Hit a problem?** Check `examples/ios-native/README.md` → "Troubleshooting" section
4. **Advanced testing?** Read `SpectraLoggerUI/TESTING.md` for comprehensive guide
5. **Integration guide?** Read `SpectraLoggerUI/README.md` for installing in your own app

---

## 📞 Need Help?

- **Example app not building?** → `examples/ios-native/README.md`
- **Swift Package tests failing?** → `SpectraLoggerUI/TESTING.md`
- **Installation questions?** → `SpectraLoggerUI/README.md`
- **General architecture?** → `PLANNING.md` & `CLAUDE.md`

---

**Last Updated**: October 27, 2025
