#!/bin/bash
set -euo pipefail

# Build all XCFrameworks for iOS distribution (Core + UI) using official KMP tasks

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_TYPE="${1:-Release}"

echo "🏗️  Building all XCFrameworks for iOS..."
echo "Project Root: $PROJECT_ROOT"
echo "Build Type: $BUILD_TYPE"

cd "$PROJECT_ROOT"

if [ "$BUILD_TYPE" == "Release" ]; then
    ./gradlew :spectra-core:assembleSpectraLoggerReleaseXCFramework :spectra-ui:assembleSpectraLoggerUIReleaseXCFramework
else
    ./gradlew :spectra-core:assembleSpectraLoggerDebugXCFramework :spectra-ui:assembleSpectraLoggerUIDebugXCFramework
fi

# Ensure output directory exists for consumers (e.g. Package.swift)
XCFRAMEWORK_DIR="$PROJECT_ROOT/build/xcframework"
mkdir -p "$XCFRAMEWORK_DIR"

# Copy the generated XCFrameworks to the centralized location
# The official KMP tasks put them in [module]/build/XCFrameworks/[type]/[name].xcframework

copy_xcframework() {
    local module=$1
    local name=$2
    local src="$PROJECT_ROOT/$module/build/XCFrameworks/${BUILD_TYPE,,}/$name.xcframework"
    local dest="$XCFRAMEWORK_DIR/$name.xcframework"
    
    if [ -d "$src" ]; then
        rm -rf "$dest"
        cp -R "$src" "$dest"
        echo "✅ $name.xcframework copied to $dest"
    else
        echo "❌ Error: $name.xcframework not found at $src"
        exit 1
    fi
}

copy_xcframework "spectra-core" "SpectraLogger"
copy_xcframework "spectra-ui" "SpectraLoggerUI"

echo "✨ All XCFrameworks prepared successfully!"
