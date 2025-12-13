#!/bin/bash
set -euo pipefail

# Validate CocoaPods podspecs
# Usage: ./validate-podspecs.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🔍 Validating CocoaPods Podspecs..."
echo ""

cd "$PROJECT_ROOT"

# Check if CocoaPods is installed
if ! command -v pod &> /dev/null; then
    echo "❌ CocoaPods not found. Please install it first:"
    echo "   sudo gem install cocoapods"
    exit 1
fi

echo "✅ CocoaPods version: $(pod --version)"
echo ""

# Validate SpectraLogger.podspec
echo "📦 Validating SpectraLogger.podspec..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if pod lib lint spectra-core/SpectraLogger.podspec --allow-warnings --verbose; then
    echo "✅ SpectraLogger.podspec is valid!"
else
    echo "❌ SpectraLogger.podspec validation failed!"
    exit 1
fi

echo ""

# Validate SpectraLoggerUI.podspec
echo "📦 Validating SpectraLoggerUI.podspec..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if pod lib lint spectra-ui-ios/SpectraLoggerUI.podspec --allow-warnings --verbose; then
    echo "✅ SpectraLoggerUI.podspec is valid!"
else
    echo "❌ SpectraLoggerUI.podspec validation failed!"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ All podspecs are valid!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Next steps:"
echo "1. Commit podspecs: git add spectra-core/SpectraLogger.podspec spectra-ui-ios/SpectraLoggerUI.podspec"
echo "2. Create release (see docs/RELEASE_PROCESS.md)"
echo "3. Publish to CocoaPods trunk (automated via CI or manual)"

exit 0
