#!/bin/bash
set -euo pipefail

# Create GitHub Release with XCFramework
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
FRAMEWORK_NAME="SpectraLogger"
TAG="v$VERSION"

echo "🚀 Creating release for version $VERSION"
echo ""

# Validate version format (semver)
if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "❌ Error: Invalid version format. Expected: X.Y.Z (e.g., 1.0.0)"
    exit 1
fi

# Navigate to project root
cd "$PROJECT_ROOT"

# Check if tag already exists
if git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "❌ Error: Tag $TAG already exists"
    exit 1
fi

# Build XCFramework
echo "📦 Building XCFramework..."
"$SCRIPT_DIR/../build/build-xcframework.sh" Release

XCFRAMEWORK_PATH="$PROJECT_ROOT/build/xcframework/$FRAMEWORK_NAME.xcframework"

# Verify XCFramework was created
if [ ! -d "$XCFRAMEWORK_PATH" ]; then
    echo "❌ Error: XCFramework not found at $XCFRAMEWORK_PATH"
    exit 1
fi

# Create releases directory
RELEASES_DIR="$PROJECT_ROOT/build/releases/$VERSION"
mkdir -p "$RELEASES_DIR"

# Zip XCFramework
echo "🗜️  Zipping XCFramework..."
ZIP_NAME="$FRAMEWORK_NAME.xcframework.zip"
ZIP_PATH="$RELEASES_DIR/$ZIP_NAME"

cd "$PROJECT_ROOT/build/xcframework"
zip -r "$ZIP_PATH" "$FRAMEWORK_NAME.xcframework"
cd "$PROJECT_ROOT"

echo "✅ Created: $ZIP_PATH"
echo "📊 Size: $(du -h "$ZIP_PATH" | cut -f1)"

# Calculate checksum
echo "🔐 Calculating SHA256 checksum..."
CHECKSUM=$(swift package compute-checksum "$ZIP_PATH")
echo "✅ Checksum: $CHECKSUM"

# Save checksum to file
CHECKSUM_FILE="$RELEASES_DIR/checksum.txt"
echo "$CHECKSUM" > "$CHECKSUM_FILE"
echo "✅ Saved checksum to: $CHECKSUM_FILE"

# Create Package.swift snippet
PACKAGE_SNIPPET_FILE="$RELEASES_DIR/Package.swift.snippet"
cat > "$PACKAGE_SNIPPET_FILE" << EOF
// Add this to your Package.swift:

.binaryTarget(
    name: "$FRAMEWORK_NAME",
    url: "https://github.com/snooky23/Spectra/releases/download/$TAG/$ZIP_NAME",
    checksum: "$CHECKSUM"
)
EOF

echo "✅ Created Package.swift snippet: $PACKAGE_SNIPPET_FILE"
cat "$PACKAGE_SNIPPET_FILE"

# Create release summary
RELEASE_SUMMARY="$RELEASES_DIR/release-summary.txt"
cat > "$RELEASE_SUMMARY" << EOF
Release: $VERSION
Tag: $TAG
Date: $(date -u +"%Y-%m-%d %H:%M:%S UTC")

Files:
- $ZIP_NAME ($(du -h "$ZIP_PATH" | cut -f1))

Checksum (SHA256):
$CHECKSUM

Next Steps:
1. Create Git tag: git tag -a $TAG -m "Release $VERSION"
2. Push tag: git push origin $TAG
3. Create GitHub Release for tag $TAG
4. Upload $ZIP_NAME to the release
5. Update Package.swift with the URL and checksum above
6. Commit and push Package.swift changes

Manual GitHub Release Creation:
gh release create $TAG "$ZIP_PATH" \\
    --title "Release $VERSION" \\
    --notes "Release notes here"

Or use the GitHub web interface.
EOF

echo ""
echo "✅ Release build complete!"
echo ""
cat "$RELEASE_SUMMARY"
echo ""
echo "📂 Release artifacts location: $RELEASES_DIR"

exit 0
