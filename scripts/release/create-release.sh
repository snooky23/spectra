#!/bin/bash
set -euo pipefail

# Create GitHub Release with all XCFrameworks (Core + UI)
# Usage: ./create-release.sh <version>
# Example: ./create-release.sh 1.0.0

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Check arguments
if [ $# -ne 1 ]; then
    echo "❌ Error: Version number required"
    echo "Usage: $0 <version>"
    echo "Example: $0 1.0.0"
    exit 1
fi

VERSION="$1"
TAG="v$VERSION"

echo "🚀 Creating release for version $VERSION"
echo ""

# Validate version format (semver)
if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "❌ Error: Invalid version format. Expected: X.Y.Z (e.g., 1.0.0)"
    exit 1
fi

cd "$PROJECT_ROOT"

# Clean previous build artifacts
if [ -d "$PROJECT_ROOT/build/releases/$VERSION" ]; then
    echo "🗑️  Cleaning previous release artifacts for $VERSION..."
    rm -rf "$PROJECT_ROOT/build/releases/$VERSION"
fi

# Build all XCFrameworks
echo "📦 Building XCFrameworks..."
"$SCRIPT_DIR/../build/build-xcframework.sh" Release

RELEASES_DIR="$PROJECT_ROOT/build/releases/$VERSION"
mkdir -p "$RELEASES_DIR"

# Function to zip and checksum a framework
package_framework() {
    local name=$1
    local zip_name="$name.xcframework.zip"
    local zip_path="$RELEASES_DIR/$zip_name"
    
    echo "🗜️  Zipping $name..."
    cd "$PROJECT_ROOT/build/xcframework"
    zip -r "$zip_path" "$name.xcframework"
    cd "$PROJECT_ROOT"
    
    echo "🔐 Calculating SHA256 checksum for $name..."
    local checksum=$(swift package compute-checksum "$zip_path")
    echo "$checksum" > "$RELEASES_DIR/$name-checksum.txt"
    echo "✅ $name Checksum: $checksum"
}

package_framework "SpectraLogger"
package_framework "SpectraLoggerUI"

# Create a combined checksum file for CI
cat "$RELEASES_DIR/SpectraLogger-checksum.txt" > "$RELEASES_DIR/checksum.txt"

echo ""
echo "✅ Release build complete!"
echo "📂 Release artifacts location: $RELEASES_DIR"

exit 0
