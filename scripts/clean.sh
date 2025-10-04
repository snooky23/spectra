#!/bin/bash

# Clean script - Removes all build artifacts
# This script cleans the project build directories

set -e  # Exit on error

# Setup Java environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java.sh"

echo "🧹 Cleaning Build Artifacts..."
echo "================================"

# Ensure gradlew is executable
chmod +x gradlew

# Clean the project
echo ""
echo "🗑️  Removing build directories..."
./gradlew clean --no-daemon

echo ""
echo "✅ Project cleaned successfully!"
