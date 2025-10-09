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
        :shared:linkReleaseFrameworkIosArm64 \
        :shared:linkReleaseFrameworkIosX64 \
        :shared:linkReleaseFrameworkIosSimulatorArm64

    FRAMEWORK_PATH="releaseFramework"
else
    ./gradlew \
        :shared:linkDebugFrameworkIosArm64 \
        :shared:linkDebugFrameworkIosX64 \
        :shared:linkDebugFrameworkIosSimulatorArm64

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

# Create XCFramework
echo "📦 Creating XCFramework..."
xcodebuild -create-xcframework \
    -framework "$PROJECT_ROOT/shared/build/bin/iosArm64/$FRAMEWORK_PATH/$FRAMEWORK_NAME.framework" \
    -framework "$PROJECT_ROOT/shared/build/bin/iosX64/$FRAMEWORK_PATH/$FRAMEWORK_NAME.framework" \
    -framework "$PROJECT_ROOT/shared/build/bin/iosSimulatorArm64/$FRAMEWORK_PATH/$FRAMEWORK_NAME.framework" \
    -output "$XCFRAMEWORK_PATH"

echo "✅ XCFramework created successfully!"
echo "📍 Location: $XCFRAMEWORK_PATH"

# Print framework info
echo ""
echo "📊 Framework Info:"
xcodebuild -version
ls -lh "$XCFRAMEWORK_PATH"
du -sh "$XCFRAMEWORK_PATH"

exit 0
