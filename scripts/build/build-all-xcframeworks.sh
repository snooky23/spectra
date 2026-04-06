#!/bin/bash
set -euo pipefail

# Build all XCFrameworks for iOS distribution (Core + UI)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_TYPE="${1:-Release}"

echo "🏗️  Building all XCFrameworks for iOS..."
echo "Project Root: $PROJECT_ROOT"
echo "Build Type: $BUILD_TYPE"

cd "$PROJECT_ROOT"

# Function to build XCFramework for a specific module
build_module_xcframework() {
    local module=$1
    local framework_name=$2
    local framework_path=""
    
    echo "📱 Building $module ($framework_name)..."
    
    if [ "$BUILD_TYPE" == "Release" ]; then
        ./gradlew \
            :$module:linkReleaseFrameworkIosArm64 \
            :$module:linkReleaseFrameworkIosX64 \
            :$module:linkReleaseFrameworkIosSimulatorArm64
        framework_path="releaseFramework"
    else
        ./gradlew \
            :$module:linkDebugFrameworkIosArm64 \
            :$module:linkDebugFrameworkIosX64 \
            :$module:linkDebugFrameworkIosSimulatorArm64
        framework_path="debugFramework"
    fi

    local XCFRAMEWORK_DIR="$PROJECT_ROOT/build/xcframework"
    mkdir -p "$XCFRAMEWORK_DIR"
    local OUTPUT_PATH="$XCFRAMEWORK_DIR/$framework_name.xcframework"
    rm -rf "$OUTPUT_PATH"

    local BIN_DIR="$PROJECT_ROOT/$module/build/bin"
    local TEMP_SIM_DIR="$PROJECT_ROOT/$module/build/temp-simulator"
    rm -rf "$TEMP_SIM_DIR"
    mkdir -p "$TEMP_SIM_DIR/$framework_name.framework"

    cp -R "$BIN_DIR/iosSimulatorArm64/$framework_path/$framework_name.framework/" "$TEMP_SIM_DIR/$framework_name.framework/"

    lipo -create \
        "$BIN_DIR/iosX64/$framework_path/$framework_name.framework/$framework_name" \
        "$BIN_DIR/iosSimulatorArm64/$framework_path/$framework_name.framework/$framework_name" \
        -output "$TEMP_SIM_DIR/$framework_name.framework/$framework_name"

    xcodebuild -create-xcframework \
        -framework "$BIN_DIR/iosArm64/$framework_path/$framework_name.framework" \
        -framework "$TEMP_SIM_DIR/$framework_name.framework" \
        -output "$OUTPUT_PATH"

    rm -rf "$TEMP_SIM_DIR"
    echo "✅ $framework_name.xcframework created at $OUTPUT_PATH"
}

# Build both modules
build_module_xcframework "spectra-core" "SpectraLogger"
build_module_xcframework "spectra-ui" "SpectraLoggerUI"

echo "✨ All XCFrameworks created successfully!"
exit 0
