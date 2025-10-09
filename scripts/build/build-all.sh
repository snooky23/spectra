#!/bin/bash
set -e

# Build everything - KMP, iOS, Android
# Main build script for CI/CD

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🚀 Building Complete Spectra Logger Project..."
echo "=============================================="

# 1. Build KMP shared library
echo ""
echo "Step 1/4: Building KMP Shared Library..."
"$SCRIPT_DIR/build-kmp.sh"

# 2. Build iOS XCFramework
echo ""
echo "Step 2/4: Building iOS XCFramework..."
"$SCRIPT_DIR/build-ios-xcframework.sh"

# 3. Build iOS example
echo ""
echo "Step 3/4: Building iOS Example..."
"$SCRIPT_DIR/build-ios-example.sh"

# 4. Build Android example
echo ""
echo "Step 4/4: Building Android Example..."
"$SCRIPT_DIR/build-android-example.sh"

echo ""
echo "=============================================="
echo "✅ All builds completed successfully!"
echo "=============================================="
