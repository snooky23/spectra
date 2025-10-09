#!/bin/bash
set -e

# Clean all build artifacts
# This removes all generated files and build outputs

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🧹 Cleaning all build artifacts..."

cd "$PROJECT_ROOT"

# Clean Gradle
echo "🗑️  Cleaning Gradle builds..."
./gradlew clean

# Clean iOS builds
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🗑️  Cleaning iOS builds..."
    rm -rf "$PROJECT_ROOT/examples/ios-native/build"
    rm -rf "$PROJECT_ROOT/examples/ios-native/DerivedData"
    rm -rf ~/Library/Developer/Xcode/DerivedData/SpectraExample-*
fi

# Clean XCFrameworks
echo "🗑️  Cleaning XCFrameworks..."
rm -rf "$PROJECT_ROOT/shared/build/XCFrameworks"

# Clean Swift Package build
echo "🗑️  Cleaning Swift Package builds..."
rm -rf "$PROJECT_ROOT/SpectraLoggerUI/.build"

echo "✅ All artifacts cleaned!"
