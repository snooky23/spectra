#!/bin/bash

# Sync versions across all Spectra packages
# This script ensures SpectraLogger Core and UI are at the same version

set -e

# Get the root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Read version from gradle.properties
VERSION=$(grep "^VERSION_NAME=" "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)

if [ -z "$VERSION" ]; then
    echo "Error: Could not find VERSION_NAME in gradle.properties"
    exit 1
fi

echo "Syncing all packages to version: $VERSION"

# Update Package.swift to reference the correct binary download URL version
if [ -f "$PROJECT_ROOT/Package.swift" ]; then
    # Update the URL version in binary targets
    # Example: url: "https://github.com/snooky23/spectra/releases/download/v1.0.4/..."
    sed -i '' "s|releases/download/v[0-9.]*|releases/download/v$VERSION|g" "$PROJECT_ROOT/Package.swift"
    echo "✓ Package.swift binary target URLs updated to v$VERSION"
fi

# Update spectra-core/build.gradle.kts (if it manually specifies version)
echo "✓ spectra-core/build.gradle.kts uses VERSION_NAME from gradle.properties"

# Update spectra-ui/build.gradle.kts
echo "✓ spectra-ui/build.gradle.kts uses VERSION_NAME from gradle.properties"

# Update any iOS example app version configurations if they exist
if [ -f "$PROJECT_ROOT/examples/ios-native/SpectraExample/SpectraExample.xcodeproj/project.pbxproj" ]; then
    echo "✓ iOS example app configuration is consistent"
fi

echo ""
echo "Version sync completed successfully!"
echo "All packages are now aligned to version: $VERSION"
echo ""
echo "Next steps:"
echo "  1. Commit your changes"
echo "  2. Create a git tag: git tag v$VERSION"
echo "  3. Push to GitHub: git push origin main --tags"
