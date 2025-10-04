#!/bin/bash

# Coverage script - Generates code coverage reports
# This script runs tests and generates coverage reports

set -e  # Exit on error

# Setup Java environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java.sh"

echo "📊 Generating Code Coverage Report..."
echo "================================"

# Ensure gradlew is executable
chmod +x gradlew

# Generate coverage report
echo ""
echo "📈 Running tests with coverage..."
./gradlew :shared:jacocoTestReport --no-daemon

echo ""
echo "✅ Coverage report generated!"
echo ""
echo "📄 View report at:"
echo "   shared/build/reports/jacoco/jacocoTestReport/html/index.html"
echo ""

# Open report in browser if available
if command -v open &> /dev/null; then
    echo "🌐 Opening report in browser..."
    open shared/build/reports/jacoco/jacocoTestReport/html/index.html
fi
