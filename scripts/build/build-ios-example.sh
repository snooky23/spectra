#!/bin/bash
set -e

# Build iOS example app
# This builds the example app which includes the SpectraLoggerUI package

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

SCHEME="SpectraExample"
PROJECT_PATH="$PROJECT_ROOT/examples/ios-native/SpectraExample.xcodeproj"
SDK="${1:-iphonesimulator}"
CONFIGURATION="${2:-Debug}"

echo "📱 Building iOS Example App..."
echo "   Scheme: $SCHEME"
echo "   SDK: $SDK"
echo "   Configuration: $CONFIGURATION"

# Ensure XCFrameworks exist
if [ ! -d "$PROJECT_ROOT/build/xcframework/SpectraLogger.xcframework" ] || [ ! -d "$PROJECT_ROOT/build/xcframework/SpectraLoggerUI.xcframework" ]; then
    echo "⚠️  XCFrameworks not found. Building them first..."
    "$SCRIPT_DIR/build-xcframework.sh"
fi

cd "$PROJECT_ROOT/examples/ios-native"

# Build
xcodebuild \
  -project "$PROJECT_PATH" \
  -scheme "$SCHEME" \
  -sdk "$SDK" \
  -configuration "$CONFIGURATION" \
  clean build \
  CODE_SIGNING_ALLOWED=NO \
  | xcpretty || xcodebuild \
  -project "$PROJECT_PATH" \
  -scheme "$SCHEME" \
  -sdk "$SDK" \
  -configuration "$CONFIGURATION" \
  clean build \
  CODE_SIGNING_ALLOWED=NO

echo "✅ iOS example app built successfully!"
