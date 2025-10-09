#!/bin/bash
set -e

# Setup development environment
# This script sets up everything needed for local development

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🔧 Setting up Spectra Logger development environment..."

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java $JAVA_VERSION installed"
else
    echo "❌ Java not found. Please install JDK 17 or higher."
    exit 1
fi

# Check Xcode (macOS only)
if [[ "$OSTYPE" == "darwin"* ]]; then
    if command -v xcodebuild &> /dev/null; then
        XCODE_VERSION=$(xcodebuild -version | head -n 1)
        echo "✅ $XCODE_VERSION installed"
    else
        echo "⚠️  Xcode not found. iOS development won't work."
    fi
fi

# Check Android SDK
if [ -n "$ANDROID_HOME" ]; then
    echo "✅ Android SDK found at $ANDROID_HOME"
else
    echo "⚠️  ANDROID_HOME not set. Android development won't work."
fi

# Build XCFramework if on macOS
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo ""
    echo "📦 Building iOS XCFramework for local development..."
    "$SCRIPT_DIR/../build/build-ios-xcframework.sh"
fi

echo ""
echo "✅ Development environment setup complete!"
echo ""
echo "Next steps:"
echo "  - iOS: Open examples/ios-native/SpectraExample.xcodeproj in Xcode"
echo "  - Android: Open project in Android Studio"
echo "  - Run tests: ./scripts/test/test-all.sh"
