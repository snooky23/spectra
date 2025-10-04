#!/bin/bash

# Format script - Auto-formats code with ktlint
# This script automatically fixes formatting issues

set -e  # Exit on error

# Setup Java environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java.sh"

echo "✨ Formatting Code..."
echo "================================"

# Ensure gradlew is executable
chmod +x gradlew

# Run ktlint format
echo ""
echo "📐 Running ktlint format..."
./gradlew ktlintFormat --no-daemon

echo ""
echo "✅ Code formatted successfully!"
