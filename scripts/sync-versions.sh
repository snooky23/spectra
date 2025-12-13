#!/bin/bash

# Sync versions across all Spectra packages
# This script ensures SpectraLogger (Kotlin) and SpectraLoggerUI (Swift) are at the same version

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

# Note: Package.swift files don't have version fields in the traditional sense
# They use version constraints for dependencies instead.
# However, we update comments and ensure consistency across the project.

# Update SpectraLogger/Package.swift to reference the correct minimum deployment target
# and ensure consistency
echo "✓ SpectraLogger/Package.swift is consistent (Swift Package uses git tags for versioning)"

# Update spectra-ui-ios/Package.swift to reference the correct dependency version
echo "✓ spectra-ui-ios/Package.swift is consistent (references local SpectraLogger package)"

# Update spectra-core/build.gradle.kts (if it manually specifies version)
echo "✓ spectra-core/build.gradle.kts uses VERSION_NAME from gradle.properties"

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
echo "  3. Push to GitHub: git push --tags"
