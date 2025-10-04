#!/bin/bash

# Build script - Replicates GitHub Actions build job
# This script builds the project and runs all tests

set -e  # Exit on error

# Setup Java environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java.sh"

echo "🔧 Building Spectra Logger..."
echo "================================"

# Ensure gradlew is executable
chmod +x gradlew

# Build the project
echo ""
echo "📦 Building project..."
./gradlew build --no-daemon --stacktrace

echo ""
echo "✅ Build completed successfully!"
