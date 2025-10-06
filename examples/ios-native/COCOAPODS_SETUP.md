# CocoaPods Setup for Local Development

This guide explains how to use CocoaPods to link the Spectra Logger framework locally for iOS development.

## Quick Start

```bash
cd examples/ios-native
./setup-pods.sh
open SpectraExample.xcworkspace
```

## How It Works

### 1. Podfile

The `Podfile` references the local shared module:

```ruby
pod 'SpectraLogger', :path => '../../shared'
```

This tells CocoaPods to use the local podspec instead of fetching from a remote repository.

### 2. SpectraLogger.podspec

Located at `shared/SpectraLogger.podspec`, this file:

- Defines the pod metadata (name, version, etc.)
- Specifies the vendored framework location
- Contains a `prepare_command` that builds the Kotlin framework

The `prepare_command` runs automatically when you do `pod install` or `pod update`:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :shared:linkDebugFrameworkIosX64
./gradlew :shared:linkDebugFrameworkIosArm64
```

### 3. Xcode Workspace

After running `pod install`, CocoaPods creates:

- `SpectraExample.xcworkspace` - **Use this to open the project**
- `Pods/` directory - Contains the pod dependencies

⚠️ **Important**: Always open `.xcworkspace`, not `.xcodeproj` when using CocoaPods!

## Workflow

### Initial Setup

```bash
# 1. Install CocoaPods (if needed)
sudo gem install cocoapods

# 2. Navigate to ios-native directory
cd examples/ios-native

# 3. Run setup script
./setup-pods.sh

# 4. Open workspace
open SpectraExample.xcworkspace
```

### After Making Changes to Shared Module

When you modify code in the `shared/` directory:

```bash
# Option 1: Update the pod (rebuilds framework)
cd examples/ios-native
pod update SpectraLogger

# Option 2: Manual build + pod update
cd ../..
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
cd examples/ios-native
pod update SpectraLogger --no-repo-update
```

### Switching Between Simulator and Device

The podspec builds frameworks for all architectures, but the default is simulator arm64 (Apple Silicon Macs).

To target a different architecture:

1. Edit `shared/SpectraLogger.podspec`
2. Change the `cp -R` command to use a different framework:
   - Simulator (Apple Silicon): `iosSimulatorArm64/debugFramework`
   - Simulator (Intel): `iosX64/debugFramework`
   - Device: `iosArm64/debugFramework`
3. Run `pod update SpectraLogger`

## Advantages of CocoaPods Setup

✅ **Automatic builds**: Framework builds automatically on `pod install/update`
✅ **Dependency management**: Easy to add more pods if needed
✅ **Team consistency**: Same setup for all developers
✅ **Standard workflow**: Familiar to iOS developers
✅ **Version control**: Can commit `Podfile` and `Podfile.lock`

## Disadvantages

❌ **Extra build step**: Need to run `pod update` after changes
❌ **Workspace overhead**: Generates additional Xcode files
❌ **Build time**: `prepare_command` runs on every `pod install`

## Alternative: Direct Framework Linking

If you prefer not to use CocoaPods, you can link the framework directly:

1. Build the framework: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
2. Use the existing Xcode project setup (see main README)
3. Add the framework to "Frameworks, Libraries, and Embedded Content"

## Troubleshooting

### "No such module 'SpectraLogger'"

**Solution**:
```bash
cd examples/ios-native
pod deintegrate
pod install
```

Then clean build folder in Xcode (Cmd+Shift+K).

### "Framework not found"

Check that the framework was built:
```bash
ls ../../shared/build/cocoapods/framework/SpectraLogger.framework
```

If missing, run:
```bash
cd ../..
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### "The sandbox is not in sync"

**Solution**:
```bash
cd examples/ios-native
pod install --repo-update
```

### Slow `pod install`

The `prepare_command` builds 3 frameworks (simulator arm64, simulator x64, device).

To speed up development, edit `SpectraLogger.podspec` and comment out the architectures you don't need.

## Files Created by CocoaPods

- `Podfile` - Pod dependencies configuration
- `Podfile.lock` - Locked versions (should be committed)
- `SpectraExample.xcworkspace` - Workspace including pods
- `Pods/` - Pod dependencies (should NOT be committed)

## Clean Setup

To completely reset the CocoaPods setup:

```bash
cd examples/ios-native

# Remove CocoaPods files
pod deintegrate
rm -rf Pods/
rm Podfile.lock

# Re-setup
./setup-pods.sh
```

## Notes

- The `vendored_frameworks` path in the podspec points to a staging directory created by the `prepare_command`
- The framework is built in debug mode; for release, update the Gradle tasks in the podspec
- CocoaPods caches can be cleared with `pod cache clean --all` if needed
