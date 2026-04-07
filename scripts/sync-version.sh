#!/bin/bash
# Sync version across all project files
# Source of truth: gradle.properties VERSION_NAME
# 
# Usage: ./scripts/sync-version.sh [version]
#   If version is provided, updates gradle.properties first
#   Then syncs that version to all other files

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

# Get or set version
if [ -n "$1" ]; then
    NEW_VERSION="$1"
    echo -e "${BLUE}Setting version to: $NEW_VERSION${NC}"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/^VERSION_NAME=.*/VERSION_NAME=$NEW_VERSION/" "$PROJECT_ROOT/gradle.properties"
    else
        sed -i "s/^VERSION_NAME=.*/VERSION_NAME=$NEW_VERSION/" "$PROJECT_ROOT/gradle.properties"
    fi
fi

# Read version from gradle.properties (source of truth)
VERSION=$(grep "^VERSION_NAME=" "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)
echo -e "${GREEN}Syncing version: $VERSION${NC}"

# Update README.md
if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s|io\.github\.snooky23:spectra-core:[0-9.]*|io.github.snooky23:spectra-core:$VERSION|g" "$PROJECT_ROOT/README.md"
    sed -i '' "s|from: \"[0-9.]*\"|from: \"$VERSION\"|g" "$PROJECT_ROOT/README.md"
else
    sed -i "s|io\.github\.snooky23:spectra-core:[0-9.]*|io.github.snooky23:spectra-core:$VERSION|g" "$PROJECT_ROOT/README.md"
    sed -i "s|from: \"[0-9.]*\"|from: \"$VERSION\"|g" "$PROJECT_ROOT/README.md"
fi
echo "✓ README.md"

# Update Package.swift (binary target URL)
if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s|releases/download/v[0-9.]*|releases/download/v$VERSION|g" "$PROJECT_ROOT/Package.swift"
else
    sed -i "s|releases/download/v[0-9.]*|releases/download/v$VERSION|g" "$PROJECT_ROOT/Package.swift"
fi
echo "✓ Package.swift"

# Update CHANGELOG.md installation examples
if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s|io\.github\.snooky23:spectra-core:[0-9.]*|io.github.snooky23:spectra-core:$VERSION|g" "$PROJECT_ROOT/CHANGELOG.md"
    sed -i '' "s|from: \"[0-9.]*\"|from: \"$VERSION\"|g" "$PROJECT_ROOT/CHANGELOG.md"
else
    sed -i "s|io\.github\.snooky23:spectra-core:[0-9.]*|io.github.snooky23:spectra-core:$VERSION|g" "$PROJECT_ROOT/CHANGELOG.md"
    sed -i "s|from: \"[0-9.]*\"|from: \"$VERSION\"|g" "$PROJECT_ROOT/CHANGELOG.md"
fi
echo "✓ CHANGELOG.md"

# Update CLAUDE.md
if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s|**Current Status**: v[0-9.]*|**Current Status**: v$VERSION|g" "$PROJECT_ROOT/CLAUDE.md"
else
    sed -i "s|**Current Status**: v[0-9.]*|**Current Status**: v$VERSION|g" "$PROJECT_ROOT/CLAUDE.md"
fi
echo "✓ CLAUDE.md"

echo -e "\n${GREEN}✅ All files synced to version $VERSION${NC}"
echo -e "${BLUE}Next steps:${NC}"
echo "  git add -A"
echo "  git commit -m \"chore: bump version to $VERSION\""
echo "  git tag v$VERSION"
echo "  git push origin main --tags"
