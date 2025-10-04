#!/bin/bash

# Code Quality script - Runs ktlint and detekt
# This script performs code quality checks

set -e  # Exit on error

# Setup Java environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java.sh"

echo "🔍 Running Code Quality Checks..."
echo "================================"

# Ensure gradlew is executable
chmod +x gradlew

# Run ktlint
echo ""
echo "📐 Running ktlint..."
./gradlew ktlintCheck --no-daemon

# Run detekt
echo ""
echo "🔎 Running detekt..."
./gradlew detekt --no-daemon

echo ""
echo "✅ All code quality checks passed!"
