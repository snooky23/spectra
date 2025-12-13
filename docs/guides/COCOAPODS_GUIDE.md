# CocoaPods Distribution Guide

This document explains how to use and distribute SpectraLogger via CocoaPods.

## Overview

SpectraLogger supports **both Swift Package Manager and CocoaPods**, ensuring compatibility with the majority of iOS projects. We provide two CocoaPods:

1. **SpectraLogger** - The core KMP framework (XCFramework)
2. **SpectraLoggerUI** - Native SwiftUI views for log visualization

## For Users: Installing via CocoaPods

### Prerequisites

- iOS 13.0+ (SpectraLogger)
- iOS 15.0+ (SpectraLoggerUI, requires SwiftUI)
- CocoaPods 1.10+

### Installation

#### 1. Install CocoaPods (if not already installed)

```bash
sudo gem install cocoapods
```

#### 2. Create or Update Podfile

```ruby
platform :ios, '15.0'
use_frameworks!

target 'YourApp' do
  # Core logging framework
  pod 'SpectraLogger', '~> 1.0'

  # Optional: Native iOS UI
  pod 'SpectraLoggerUI', '~> 1.0'
end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'
    end
  end
end
```

#### 3. Install Pods

```bash
pod install
```

#### 4. Open Workspace

```bash
open YourApp.xcworkspace
```

### Usage

```swift
import SpectraLogger
import SpectraLoggerUI // If using UI

// Initialize logger
let logger = SpectraLogger.shared

// Log messages
logger.info("App started")
logger.debug("Debug info")
logger.error("Something went wrong")

// Show UI (if using SpectraLoggerUI)
import SwiftUI

struct ContentView: View {
    var body: some View {
        SpectraLoggerView()
    }
}
```

---

## For Maintainers: Publishing to CocoaPods

### Prerequisites

1. **CocoaPods Account**
   ```bash
   # Register with CocoaPods (one-time)
   pod trunk register aviavi23@gmail.com 'Avi Levin' --description='MacBook Pro'

   # Verify email from CocoaPods
   # Check your registration
   pod trunk me
   ```

2. **GitHub Personal Access Token** (for CI/CD)
   - Go to GitHub Settings → Developer Settings → Personal Access Tokens
   - Create token with `repo` scope
   - Add as `COCOAPODS_TRUNK_TOKEN` secret in GitHub repository

### Manual Publishing Process

#### 1. Validate Podspecs

```bash
# Validate SpectraLogger
cd shared
pod spec lint SpectraLogger.podspec --allow-warnings

# Validate SpectraLoggerUI
cd ../SpectraLoggerUI
pod spec lint SpectraLoggerUI.podspec --allow-warnings
```

#### 2. Update Podspec Versions

Edit `shared/SpectraLogger.podspec`:
```ruby
spec.version = '1.0.0'
```

Edit `SpectraLoggerUI/SpectraLoggerUI.podspec`:
```ruby
spec.version = '1.0.0'
```

#### 3. Create GitHub Release

First, create a GitHub release with the XCFramework (see RELEASE_PROCESS.md):

```bash
# Create tag
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# CI/CD will create GitHub Release with XCFramework
```

#### 4. Publish to CocoaPods Trunk

After the GitHub Release is created:

```bash
# Publish SpectraLogger (must be published first)
cd shared
pod trunk push SpectraLogger.podspec --allow-warnings

# Publish SpectraLoggerUI (depends on SpectraLogger)
cd ../SpectraLoggerUI
pod trunk push SpectraLoggerUI.podspec --allow-warnings
```

#### 5. Verify Publication

```bash
# Search for your pod
pod search SpectraLogger

# Check pod info
pod spec cat SpectraLogger
pod spec cat SpectraLoggerUI
```

### Automated Publishing (CI/CD)

The GitHub Actions workflow `.github/workflows/release.yml` automatically publishes to CocoaPods when you push a version tag.

**Setup:**

1. Get your CocoaPods token:
   ```bash
   pod trunk me --verbose
   ```

2. Add to GitHub Secrets:
   - Go to GitHub repo → Settings → Secrets and Variables → Actions
   - Add new secret: `COCOAPODS_TRUNK_TOKEN`
   - Paste your token

3. Create release:
   ```bash
   git tag -a v1.0.0 -m "Release 1.0.0"
   git push origin v1.0.0
   ```

The workflow will:
1. ✅ Build XCFramework
2. ✅ Create GitHub Release
3. ✅ Validate podspecs
4. ✅ Publish to CocoaPods Trunk automatically

---

## Podspec Structure

### SpectraLogger.podspec (Core Framework)

```ruby
Pod::Spec.new do |spec|
  spec.name         = 'SpectraLogger'
  spec.version      = '1.0.0'
  spec.homepage     = 'https://github.com/snooky23/spectra'

  # Downloads pre-built XCFramework from GitHub Releases
  spec.source       = {
    :http => "https://github.com/snooky23/spectra/releases/download/v#{spec.version}/SpectraLogger.xcframework.zip"
  }

  # Use vendored XCFramework (no build required)
  spec.vendored_frameworks = 'SpectraLogger.xcframework'

  spec.ios.deployment_target = '13.0'
  spec.swift_version = '5.9'
end
```

**Key Points:**
- ✅ Downloads from GitHub Releases (no local build)
- ✅ Uses `:http` source for binary distribution
- ✅ Minimal installation time for users
- ✅ Matches SPM distribution strategy

### SpectraLoggerUI.podspec (Native UI)

```ruby
Pod::Spec.new do |spec|
  spec.name         = 'SpectraLoggerUI'
  spec.version      = '1.0.0'

  # Downloads source code from Git
  spec.source       = {
    :git => 'https://github.com/snooky23/spectra.git',
    :tag => "v#{spec.version}"
  }

  # Swift source files
  spec.source_files = 'SpectraLoggerUI/Sources/**/*.swift'

  # Depends on core framework
  spec.dependency 'SpectraLogger', '~> 1.0'

  spec.ios.deployment_target = '15.0'
  spec.swift_version = '5.9'
  spec.frameworks = 'SwiftUI', 'Combine'
end
```

**Key Points:**
- ✅ Source distribution (Swift code)
- ✅ Depends on SpectraLogger pod
- ✅ Requires iOS 15+ for SwiftUI
- ✅ Automatically compiles with user's project

---

## Versioning Strategy

Follow **semantic versioning** and keep versions in sync:

| Component | Version | Update When |
|-----------|---------|-------------|
| SpectraLogger.podspec | 1.0.0 | Core framework changes |
| SpectraLoggerUI.podspec | 1.0.0 | UI changes or core dependency update |
| GitHub Release Tag | v1.0.0 | Every release |

**Important:** SpectraLoggerUI should depend on the same major version:
```ruby
spec.dependency 'SpectraLogger', '~> 1.0'  # Allows 1.x.x
```

---

## Testing Podspecs Locally

### Test SpectraLogger Installation

```bash
# Create test Podfile
cat > Podfile << 'EOF'
platform :ios, '15.0'
use_frameworks!

target 'TestApp' do
  pod 'SpectraLogger', :path => './shared'
end
EOF

# Install and test
pod install
```

### Test SpectraLoggerUI Installation

```bash
cat > Podfile << 'EOF'
platform :ios, '15.0'
use_frameworks!

target 'TestApp' do
  pod 'SpectraLogger', :path => './shared'
  pod 'SpectraLoggerUI', :path => './SpectraLoggerUI'
end
EOF

pod install
```

### Lint Podspecs

```bash
# Lint individual podspecs
pod spec lint shared/SpectraLogger.podspec --allow-warnings
pod spec lint SpectraLoggerUI/SpectraLoggerUI.podspec --allow-warnings

# Quick validation (faster)
pod lib lint shared/SpectraLogger.podspec --allow-warnings
pod lib lint SpectraLoggerUI/SpectraLoggerUI.podspec --allow-warnings
```

---

## Troubleshooting

### "Unable to find a specification for SpectraLogger"

**Problem:** Pod not found in CocoaPods repository.

**Solutions:**
1. Update CocoaPods repo:
   ```bash
   pod repo update
   pod search SpectraLogger
   ```
2. Check if pod is published:
   ```bash
   pod trunk me
   ```
3. Verify podspec on GitHub

### "The platform of the target... is not compatible"

**Problem:** Deployment target mismatch.

**Solution:** Update Podfile:
```ruby
post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'
    end
  end
end
```

### "Unable to download XCFramework"

**Problem:** GitHub Release or XCFramework missing.

**Solutions:**
1. Verify GitHub Release exists
2. Check XCFramework was uploaded
3. Verify URL in podspec matches release
4. Check internet connectivity

### Podspec Validation Fails

**Problem:** `pod spec lint` reports errors.

**Solutions:**
1. Check all URLs are valid
2. Ensure XCFramework exists at source URL
3. Verify Swift version compatibility
4. Use `--allow-warnings` for non-critical warnings
5. Check dependency versions

---

## Best Practices

### For Users

- ✅ Use specific version ranges: `pod 'SpectraLogger', '~> 1.0'`
- ✅ Update pods regularly: `pod update SpectraLogger`
- ✅ Commit `Podfile.lock` to version control
- ✅ Use `pod install` after cloning, not `pod update`

### For Maintainers

- ✅ Always validate before publishing: `pod spec lint`
- ✅ Test podspecs locally before releasing
- ✅ Keep versions synchronized across podspecs
- ✅ Include detailed changelog in releases
- ✅ Never delete published pods (deprecate instead)

### Deprecation

If you need to deprecate a version:

```bash
# Deprecate a specific version
pod trunk deprecate SpectraLogger --version=0.5.0

# Deprecate in favor of newer pod
pod trunk deprecate SpectraLogger --in-favor-of=SpectraLoggerV2
```

---

## Comparison: SPM vs CocoaPods

| Feature | Swift Package Manager | CocoaPods |
|---------|----------------------|-----------|
| Setup | ✅ Built into Xcode | ⚠️ Requires gem install |
| Speed | ✅ Faster | ⚠️ Slower (Pods workspace) |
| Adoption | 📈 Growing (50%+) | 📊 Established (70%+) |
| Binary Support | ✅ XCFramework | ✅ XCFramework |
| Source Support | ✅ Native | ✅ Native |
| Version Resolution | ✅ Automatic | ✅ Automatic |
| Best For | New projects | Legacy projects |

**Recommendation:** Support both! SpectraLogger does.

---

## Migration Guide

### From CocoaPods to SPM

1. Remove from Podfile:
   ```ruby
   # Remove these lines
   pod 'SpectraLogger'
   pod 'SpectraLoggerUI'
   ```

2. Run:
   ```bash
   pod deintegrate
   ```

3. Add via Xcode (see SPM_DISTRIBUTION_GUIDE.md)

4. Update imports (no change needed - same module names)

### From SPM to CocoaPods

1. Remove SPM dependency from Xcode
2. Create Podfile (see installation section above)
3. Run `pod install`
4. Open `.xcworkspace` file

---

## Support

For issues:
- GitHub Issues: https://github.com/snooky23/spectra/issues
- CocoaPods: https://trunk.cocoapods.org/pods/SpectraLogger

---

**Document Version:** 1.0
**Last Updated:** 2025-10-08
**CocoaPods Compatibility:** 1.10+
