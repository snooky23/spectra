#!/bin/bash
# =============================================================================
# Release Script
# =============================================================================
# Automates the release process:
# 1. Updates CHANGELOG.md (moves Unreleased to new version)
# 2. Updates version numbers in build files
# 3. Creates git tag
# 4. Optionally pushes to trigger CI/CD
#
# Usage:
#   ./scripts/release.sh 0.1.0           # Prepare release
#   ./scripts/release.sh 0.1.0 --push    # Prepare and push
# =============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Arguments
VERSION=$1
PUSH_FLAG=$2

if [ -z "$VERSION" ]; then
    echo -e "${RED}Error: Version number required${NC}"
    echo "Usage: ./scripts/release.sh <version> [--push]"
    echo "Example: ./scripts/release.sh 0.1.0"
    exit 1
fi

# Validate semantic versioning format
if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo -e "${RED}Error: Version must be in format X.Y.Z (e.g., 0.1.0)${NC}"
    exit 1
fi

echo -e "${GREEN}🚀 Preparing release v${VERSION}${NC}"
echo ""

# =============================================================================
# Step 1: Update CHANGELOG.md
# =============================================================================
echo -e "${YELLOW}📝 Updating CHANGELOG.md...${NC}"

DATE=$(date +%Y-%m-%d)
CHANGELOG="CHANGELOG.md"

if [ ! -f "$CHANGELOG" ]; then
    echo -e "${RED}Error: CHANGELOG.md not found${NC}"
    exit 1
fi

# Check if there are unreleased changes
if ! grep -q "## \[Unreleased\]" "$CHANGELOG"; then
    echo -e "${RED}Error: No [Unreleased] section found in CHANGELOG.md${NC}"
    exit 1
fi

# Replace [Unreleased] with version and add new Unreleased section
sed -i '' "s/## \[Unreleased\]/## [Unreleased]\n\n---\n\n## [$VERSION] - $DATE/" "$CHANGELOG"

# Update comparison links at bottom
if grep -q "\[Unreleased\]:" "$CHANGELOG"; then
    # Update the unreleased comparison link
    sed -i '' "s|\[Unreleased\]:.*|[Unreleased]: https://github.com/snooky23/spectra/compare/v${VERSION}...HEAD|" "$CHANGELOG"
    
    # Add new version link before the first version link
    sed -i '' "/\[${VERSION}\]/!s|\(\[Unreleased\]:.*\)|\1\n[${VERSION}]: https://github.com/snooky23/spectra/compare/v0.0.1...v${VERSION}|" "$CHANGELOG"
fi

echo -e "${GREEN}✓ CHANGELOG.md updated${NC}"

# =============================================================================
# Step 2: Update version in build files
# =============================================================================
echo -e "${YELLOW}📦 Updating version numbers...${NC}"

# Update gradle.properties or libs.versions.toml
if [ -f "gradle/libs.versions.toml" ]; then
    sed -i '' "s/spectra = \".*\"/spectra = \"$VERSION\"/" gradle/libs.versions.toml
    echo -e "${GREEN}✓ Updated gradle/libs.versions.toml${NC}"
fi

# Update Package.swift version (if using version constant)
# Note: Swift packages typically use git tags, not embedded versions

echo -e "${GREEN}✓ Version numbers updated${NC}"

# =============================================================================
# Step 3: Commit and tag
# =============================================================================
echo -e "${YELLOW}🏷️  Creating git tag...${NC}"

git add -A
git commit -m "chore: release v${VERSION}

- Update CHANGELOG.md
- Bump version numbers"

git tag -a "v${VERSION}" -m "Release v${VERSION}"

echo -e "${GREEN}✓ Created tag v${VERSION}${NC}"

# =============================================================================
# Step 4: Push (optional)
# =============================================================================
if [ "$PUSH_FLAG" = "--push" ]; then
    echo -e "${YELLOW}🚢 Pushing to remote...${NC}"
    git push origin main
    git push origin "v${VERSION}"
    echo -e "${GREEN}✓ Pushed to remote${NC}"
    echo ""
    echo -e "${GREEN}🎉 Release v${VERSION} published!${NC}"
    echo "GitHub Actions will now build and publish the release."
else
    echo ""
    echo -e "${YELLOW}To complete the release, run:${NC}"
    echo "  git push origin main"
    echo "  git push origin v${VERSION}"
fi

echo ""
echo -e "${GREEN}✅ Release preparation complete!${NC}"
