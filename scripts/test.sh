#!/bin/bash

# Test script - Runs all tests
# This script runs the complete test suite

set -e  # Exit on error

# Setup Java environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java.sh"

echo "🧪 Running Tests..."
echo "================================"

# Ensure gradlew is executable
chmod +x gradlew

# Run all tests
echo ""
echo "📝 Running test suite..."
./gradlew test --no-daemon --stacktrace

echo ""
echo "✅ All tests passed!"
