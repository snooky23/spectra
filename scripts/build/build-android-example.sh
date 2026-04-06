#!/bin/bash
set -e

# Build Android example app
# This builds the Android example application

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

VARIANT="${1:-debug}"

echo "🤖 Building Android Example App..."
echo "   Variant: $VARIANT"

cd "$PROJECT_ROOT"

# Build the shared modules first
echo "📦 Building shared modules..."
./gradlew :spectra-core:assembleRelease :spectra-ui:assembleRelease

# Build Android example
echo "📱 Building Android app..."
if [ "$VARIANT" = "release" ]; then
    ./gradlew :examples:android-native:app:assembleRelease
else
    ./gradlew :examples:android-native:app:assembleDebug
fi

echo "✅ Android example app built successfully!"
