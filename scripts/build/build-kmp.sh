#!/bin/bash
set -e

# Build Kotlin Multiplatform shared library
# This script builds the KMP shared module for all platforms

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🔨 Building Kotlin Multiplatform Shared Library..."
echo "Project root: $PROJECT_ROOT"

cd "$PROJECT_ROOT"

# Clean build
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build for all platforms
echo "📦 Building for Android..."
./gradlew :shared:assembleRelease

echo "📦 Building for iOS..."
./gradlew :shared:linkReleaseFrameworkIosArm64 \
         :shared:linkReleaseFrameworkIosSimulatorArm64 \
         :shared:linkReleaseFrameworkIosX64

echo "✅ KMP build completed successfully!"
