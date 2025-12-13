#!/bin/bash
set -e

# Run KMP tests for all platforms
# This runs unit tests in commonTest and platform-specific tests

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🧪 Running KMP Tests..."

cd "$PROJECT_ROOT"

# Run common tests
echo "📋 Running common tests..."
./gradlew :spectra-core:allTests

echo "✅ All KMP tests passed!"
