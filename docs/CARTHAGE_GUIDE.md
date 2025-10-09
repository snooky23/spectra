# Carthage Distribution Guide

This document explains how to use SpectraLogger with Carthage, a decentralized dependency manager for iOS.

## Overview

SpectraLogger provides **binary XCFramework distribution** via Carthage for fast, lightweight integration.

**Supported**:
- ✅ SpectraLogger (Core framework) - 8.5MB binary

**Not Supported**:
- ❌ SpectraLoggerUI (SwiftUI source-only, use SPM or CocoaPods)

---

## For Users: Installing via Carthage

### Prerequisites

- macOS with Xcode 12+ (XCFramework support)
- Carthage 0.37.0+ (XCFramework support)
- iOS 13.0+ deployment target

### Installation

#### 1. Install Carthage (if needed)

```bash
brew install carthage
```

Verify installation:
```bash
carthage version
# Should be 0.37.0 or higher
```

#### 2. Create Cartfile

In your project root:

```ruby
# Binary distribution (pre-built XCFramework)
binary "https://raw.githubusercontent.com/snooky23/Spectra/main/SpectraLogger.json" ~> 1.0
```

**Why binary?**
- ⚡ 10x faster than building from source
- 📦 Smaller download (8.5MB vs 50MB+ source)
- ✅ Pre-tested release builds

#### 3. Update Dependencies

```bash
carthage update --use-xcframeworks --platform iOS
```

**Flags explained**:
- `--use-xcframeworks`: Required for XCFramework support (Xcode 12+)
- `--platform iOS`: Build only for iOS (faster)

This downloads the pre-built XCFramework to `Carthage/Build/SpectraLogger.xcframework`.

#### 4. Add to Xcode Project

##### Option A: Drag & Drop (Recommended)

1. Open your `.xcodeproj` in Xcode
2. Navigate to your app target → **General** tab
3. Scroll to **Frameworks, Libraries, and Embedded Content**
4. Drag `Carthage/Build/SpectraLogger.xcframework` from Finder
5. Set to **"Embed & Sign"**

##### Option B: Manual Linking

1. Target → **Build Phases** → **Link Binary With Libraries**
2. Click `+` → **Add Other** → **Add Files**
3. Select `Carthage/Build/SpectraLogger.xcframework`
4. Target → **General** → **Frameworks, Libraries, and Embedded Content**
5. Find SpectraLogger.xcframework, set to **"Embed & Sign"**

#### 5. Add Copy Frameworks Script (Optional but Recommended)

This script strips unnecessary architectures on build:

1. Target → **Build Phases** → **+** → **New Run Script Phase**
2. Rename to "Copy Carthage Frameworks"
3. **Shell**: `/bin/sh`
4. **Script**:
   ```bash
   /usr/local/bin/carthage copy-frameworks
   ```
5. **Input Files**:
   ```
   $(SRCROOT)/Carthage/Build/SpectraLogger.xcframework
   ```

---

## Usage

### Basic Setup

```swift
import UIKit
import SpectraLogger

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication,
                    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        // Initialize SpectraLogger
        SpectraLogger.configure(
            maxInMemoryLogs: 1000,
            enableFileLogging: true,
            enableNetworkLogging: true
        )

        return true
    }
}
```

### Logging

```swift
import SpectraLogger

class ViewController: UIViewController {
    private let logger = SpectraLogger.getLogger(category: "MyApp")

    override func viewDidLoad() {
        super.viewDidLoad()

        logger.info("ViewController loaded")
        logger.debug("Setting up UI...")

        // Network requests are automatically logged
        URLSession.shared.dataTask(with: URL(string: "https://api.example.com")!) { data, response, error in
            // Request/response logged automatically
        }.resume()
    }
}
```

---

## Updating Dependencies

### Update to Latest Version

```bash
carthage update SpectraLogger --use-xcframeworks --platform iOS
```

### Update to Specific Version

Edit `Cartfile`:
```ruby
binary "https://raw.githubusercontent.com/snooky23/Spectra/main/SpectraLogger.json" == 1.0.0
```

Then run:
```bash
carthage update --use-xcframeworks --platform iOS
```

### Check Installed Version

```bash
carthage outdated
```

---

## Troubleshooting

### "Failed to download binary"

**Problem**: Carthage can't download the XCFramework.

**Solutions**:
```bash
# 1. Verify JSON manifest is accessible
curl https://raw.githubusercontent.com/snooky23/Spectra/main/SpectraLogger.json

# 2. Clear Carthage cache
rm -rf ~/Library/Caches/org.carthage.CarthageKit

# 3. Try again
carthage update --use-xcframeworks --platform iOS
```

### "XCFramework not found in Carthage/Build"

**Problem**: `carthage update` completed but XCFramework is missing.

**Solution**:
```bash
# Ensure you're using --use-xcframeworks flag
carthage update --use-xcframeworks --platform iOS

# Check Carthage version (must be 0.37.0+)
carthage version

# Update Carthage if needed
brew upgrade carthage
```

### "Module 'SpectraLogger' not found"

**Problem**: Xcode can't find the framework.

**Solutions**:
1. **Verify embedding**: Target → General → Frameworks, Libraries, and Embedded Content
   - SpectraLogger.xcframework should be listed
   - Set to "Embed & Sign" (not "Do Not Embed")

2. **Clean and rebuild**:
   ```
   Product → Clean Build Folder (Cmd+Shift+K)
   Product → Build (Cmd+B)
   ```

3. **Check Framework Search Paths**:
   - Target → Build Settings → Search "Framework Search Paths"
   - Should include: `$(PROJECT_DIR)/Carthage/Build`

### "Unsupported architecture i386"

**Problem**: Building for 32-bit simulator (obsolete).

**Solution**: SpectraLogger only supports 64-bit architectures:
- ✅ arm64 (real devices)
- ✅ x86_64 (Intel Mac simulators)
- ✅ arm64 (Apple Silicon Mac simulators)
- ❌ i386 (32-bit simulators - obsolete since iOS 11)

Select a modern simulator (iPhone 6s or newer).

### "The binary project at ... doesn't have a release"

**Problem**: Version specified in Cartfile doesn't exist.

**Solution**:
```bash
# Check available versions
curl https://raw.githubusercontent.com/snooky23/Spectra/main/SpectraLogger.json

# Use a valid version in Cartfile
binary "https://raw.githubusercontent.com/snooky23/Spectra/main/SpectraLogger.json" ~> 1.0
```

---

## Best Practices

### ✅ Do's

- **Commit `Cartfile` and `Cartfile.resolved`** to version control
  - Ensures consistent builds across team
  - `Cartfile.resolved` locks exact versions

- **Use version constraints**:
  ```ruby
  ~> 1.0    # Any version >= 1.0 and < 2.0 (recommended)
  == 1.0.0  # Exact version (for strict control)
  >= 1.0    # Minimum version (flexible)
  ```

- **Run `carthage update` periodically**
  - Get bug fixes and improvements
  - Check release notes before updating

- **Add Carthage/ to .gitignore**:
  ```
  # Carthage
  Carthage/Build
  Carthage/Checkouts
  ```

### ❌ Don'ts

- **Don't commit `Carthage/Build` or `Carthage/Checkouts`**
  - Large binary files bloat your repo
  - Different developers may use different architectures

- **Don't forget the `--use-xcframeworks` flag**
  - Old framework format is deprecated
  - XCFramework supports all architectures

- **Don't manually modify `Cartfile.resolved`**
  - Auto-generated by Carthage
  - Hand edits will be overwritten

---

## Migration

### From CocoaPods to Carthage

1. **Remove CocoaPods**:
   ```bash
   pod deintegrate
   rm Podfile Podfile.lock
   rm -rf Pods/
   ```

2. **Create Cartfile**:
   ```ruby
   binary "https://raw.githubusercontent.com/snooky23/Spectra/main/SpectraLogger.json" ~> 1.0
   ```

3. **Install with Carthage**:
   ```bash
   carthage update --use-xcframeworks --platform iOS
   ```

4. **Add XCFramework to Xcode** (see installation steps above)

5. **Update imports** (no changes needed - same module name):
   ```swift
   import SpectraLogger  // Works the same!
   ```

### From SPM to Carthage

1. **Remove SPM dependency**:
   - Xcode → File → Packages → Remove "Spectra"

2. **Follow Carthage installation steps above**

### From Carthage to SPM/CocoaPods

See [INSTALLATION.md](./INSTALLATION.md#migration-between-methods) for migration guides.

---

## Comparison: Carthage vs Other Methods

| Feature | SPM | CocoaPods | Carthage |
|---------|-----|-----------|----------|
| **Xcode Integration** | ✅ Built-in | ⚠️ Requires pod install | 🔧 Manual |
| **Build Time** | Fast | Moderate | Fastest (pre-built) |
| **Setup Complexity** | Easy | Moderate | Moderate |
| **Dependency Resolution** | Automatic | Automatic | Manual |
| **Binary Support** | ✅ XCFramework | ✅ XCFramework | ✅ XCFramework |
| **Decentralized** | ❌ | ❌ | ✅ |
| **Version Locking** | ✅ Package.resolved | ✅ Podfile.lock | ✅ Cartfile.resolved |
| **Best For** | New projects | Legacy projects | Enterprise control |

**Why Choose Carthage?**
- 🏢 **Enterprise**: Manual control over dependencies
- ⚡ **Performance**: Pre-built binaries (no compile time)
- 🔒 **Security**: Review binaries before integration
- 🎯 **Simplicity**: No magic, just frameworks

---

## For Maintainers: Publishing to Carthage

See [RELEASE_PROCESS.md](./RELEASE_PROCESS.md#carthage-distribution) for instructions on updating the binary JSON manifest.

---

## Additional Resources

- [Carthage Documentation](https://github.com/Carthage/Carthage)
- [XCFramework Guide](https://developer.apple.com/documentation/xcode/creating-a-multi-platform-binary-framework-bundle)
- [Installation Guide](./INSTALLATION.md)
- [Release Process](./RELEASE_PROCESS.md)

---

## Support

- 📘 Documentation: https://github.com/snooky23/Spectra/tree/main/docs
- 🐛 Issues: https://github.com/snooky23/Spectra/issues
- 💬 Discussions: https://github.com/snooky23/Spectra/discussions

---

**Document Version:** 1.0
**Last Updated:** 2025-10-09
**Carthage Version:** 0.37.0+
**SpectraLogger Version:** 1.0.0
