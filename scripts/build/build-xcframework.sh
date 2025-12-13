#!/bin/bash
set -euo pipefail

# Build XCFramework for iOS distribution
# This script builds the Kotlin Multiplatform framework for all iOS targets
# and packages them into a single XCFramework

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_TYPE="${1:-Release}"
FRAMEWORK_NAME="SpectraLogger"

echo "🏗️  Building XCFramework for iOS..."
echo "Project Root: $PROJECT_ROOT"
echo "Build Type: $BUILD_TYPE"

# Navigate to project root
cd "$PROJECT_ROOT"

# Build all iOS targets
echo "📱 Building iOS targets..."
if [ "$BUILD_TYPE" == "Release" ]; then
    ./gradlew \
        :spectra-core:linkReleaseFrameworkIosArm64 \
        :spectra-core:linkReleaseFrameworkIosX64 \
        :spectra-core:linkReleaseFrameworkIosSimulatorArm64

    FRAMEWORK_PATH="releaseFramework"
else
    ./gradlew \
        :spectra-core:linkDebugFrameworkIosArm64 \
        :spectra-core:linkDebugFrameworkIosX64 \
        :spectra-core:linkDebugFrameworkIosSimulatorArm64

    FRAMEWORK_PATH="debugFramework"
fi

# Create XCFramework output directory
XCFRAMEWORK_DIR="$PROJECT_ROOT/build/xcframework"
mkdir -p "$XCFRAMEWORK_DIR"

# Remove old XCFramework if exists
XCFRAMEWORK_PATH="$XCFRAMEWORK_DIR/$FRAMEWORK_NAME.xcframework"
if [ -d "$XCFRAMEWORK_PATH" ]; then
    echo "🗑️  Removing old XCFramework..."
    rm -rf "$XCFRAMEWORK_PATH"
fi

# Create fat binary for simulator (combines x86_64 and arm64)
echo "🔨 Creating simulator fat binary..."
BUILD_DIR="$PROJECT_ROOT/spectra-core/build/bin"
TEMP_SIMULATOR_DIR="$PROJECT_ROOT/spectra-core/build/temp-simulator"
rm -rf "$TEMP_SIMULATOR_DIR"
mkdir -p "$TEMP_SIMULATOR_DIR/$FRAMEWORK_NAME.framework"

# Copy the framework structure from one of the simulator builds
cp -R "$BUILD_DIR/iosSimulatorArm64/$FRAMEWORK_PATH/$FRAMEWORK_NAME.framework/" "$TEMP_SIMULATOR_DIR/$FRAMEWORK_NAME.framework/"

# Create fat binary combining both simulator architectures
lipo -create \
    "$BUILD_DIR/iosX64/$FRAMEWORK_PATH/$FRAMEWORK_NAME.framework/$FRAMEWORK_NAME" \
    "$BUILD_DIR/iosSimulatorArm64/$FRAMEWORK_PATH/$FRAMEWORK_NAME.framework/$FRAMEWORK_NAME" \
    -output "$TEMP_SIMULATOR_DIR/$FRAMEWORK_NAME.framework/$FRAMEWORK_NAME"

echo "✅ Simulator fat binary created"

# Create XCFramework with device and simulator (fat binary)
echo "📦 Creating XCFramework..."
xcodebuild -create-xcframework \
    -framework "$BUILD_DIR/iosArm64/$FRAMEWORK_PATH/$FRAMEWORK_NAME.framework" \
    -framework "$TEMP_SIMULATOR_DIR/$FRAMEWORK_NAME.framework" \
    -output "$XCFRAMEWORK_PATH"

# Clean up temp directory
rm -rf "$TEMP_SIMULATOR_DIR"

echo "✅ XCFramework created successfully!"
echo "📍 Location: $XCFRAMEWORK_PATH"

# Print framework info
echo ""
echo "📊 Framework Info:"
xcodebuild -version
ls -lh "$XCFRAMEWORK_PATH"
du -sh "$XCFRAMEWORK_PATH"

exit 0
