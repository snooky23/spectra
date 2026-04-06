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
./gradlew :spectra-core:assembleRelease :spectra-ui:assembleRelease

echo "📦 Building for iOS..."
./gradlew :spectra-core:linkReleaseFrameworkIosArm64 \
         :spectra-core:linkReleaseFrameworkIosSimulatorArm64 \
         :spectra-core:linkReleaseFrameworkIosX64 \
         :spectra-ui:linkReleaseFrameworkIosArm64 \
         :spectra-ui:linkReleaseFrameworkIosSimulatorArm64 \
         :spectra-ui:linkReleaseFrameworkIosX64

echo "✅ KMP build completed successfully!"
