#!/bin/bash
set -e

# Build Kotlin Multiplatform shared modules
# This script builds the KMP modules for all platforms

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🔨 Building Spectra Logger KMP Modules..."
echo "Project root: $PROJECT_ROOT"

cd "$PROJECT_ROOT"

# Clean build
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build all modules (Core + UI)
# This handles both Android and iOS targets
echo "📦 Building all modules..."
./gradlew :spectra-core:assemble :spectra-ui:assemble

echo "✅ KMP build completed successfully!"
