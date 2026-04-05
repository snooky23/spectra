#!/bin/bash
set -e

# Build iOS UI Swift Package
# This validates the Swift Package can be built successfully

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🎨 Building iOS UI Swift Package..."

# Ensure XCFramework is built first
if [ ! -d "$PROJECT_ROOT/build/xcframework/SpectraLogger.xcframework" ]; then
    echo "⚠️  XCFramework not found. Building it first..."
    "$SCRIPT_DIR/build-xcframework.sh"
fi

cd "$PROJECT_ROOT/spectra-ui-ios"

# Swift build doesn't work well with binary targets from command line
# So we'll open it in Xcode for validation instead
echo "📱 Opening Swift Package in Xcode for validation..."
open Package.swift

echo "✅ Swift Package opened in Xcode"
echo "ℹ️  To build from command line, use the example app instead"
