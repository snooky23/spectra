#!/bin/bash
set -e

# Run Android tests
# This runs unit tests for Android

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🧪 Running Android Tests..."

cd "$PROJECT_ROOT"

# Run unit tests
echo "📋 Running Android unit tests..."
./gradlew :examples:android:testDebugUnitTest

echo "✅ Android tests passed!"
