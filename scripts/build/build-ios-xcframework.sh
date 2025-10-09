#!/bin/bash
set -e

# Build iOS XCFramework from KMP frameworks
# This creates a universal XCFramework for distribution

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

FRAMEWORK_NAME="SpectraLogger"
BUILD_DIR="$PROJECT_ROOT/shared/build"
XCFRAMEWORK_PATH="$BUILD_DIR/XCFrameworks/release/$FRAMEWORK_NAME.xcframework"

echo "🍎 Building iOS XCFramework..."

# Build individual frameworks first
echo "📦 Building iOS frameworks..."
"$SCRIPT_DIR/build-kmp.sh"

# Create output directory
mkdir -p "$BUILD_DIR/XCFrameworks/release"

# Remove old XCFramework if exists
if [ -d "$XCFRAMEWORK_PATH" ]; then
    echo "🗑️  Removing old XCFramework..."
    rm -rf "$XCFRAMEWORK_PATH"
fi

# Combine simulator architectures into FAT binary
echo "🔄 Creating FAT binary for simulators..."
lipo -create \
  "$BUILD_DIR/bin/iosSimulatorArm64/releaseFramework/$FRAMEWORK_NAME.framework/$FRAMEWORK_NAME" \
  "$BUILD_DIR/bin/iosX64/releaseFramework/$FRAMEWORK_NAME.framework/$FRAMEWORK_NAME" \
  -output "$BUILD_DIR/bin/iosX64/releaseFramework/$FRAMEWORK_NAME.framework/$FRAMEWORK_NAME"

# Create XCFramework
echo "📦 Creating XCFramework..."
xcodebuild -create-xcframework \
  -framework "$BUILD_DIR/bin/iosArm64/releaseFramework/$FRAMEWORK_NAME.framework" \
  -framework "$BUILD_DIR/bin/iosX64/releaseFramework/$FRAMEWORK_NAME.framework" \
  -output "$XCFRAMEWORK_PATH"

echo "✅ XCFramework created at: $XCFRAMEWORK_PATH"

# Print size
du -sh "$XCFRAMEWORK_PATH"
