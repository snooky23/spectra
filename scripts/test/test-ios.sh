#!/bin/bash
set -e

# Run iOS tests
# This runs unit tests for the iOS example app

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

SCHEME="SpectraExample"
PROJECT_PATH="$PROJECT_ROOT/examples/ios-native/SpectraExample.xcodeproj"
DESTINATION="${1:-platform=iOS Simulator,name=iPhone 15,OS=latest}"

echo "🧪 Running iOS Tests..."
echo "   Destination: $DESTINATION"

cd "$PROJECT_ROOT/examples/ios-native"

# Run tests
xcodebuild test \
  -project "$PROJECT_PATH" \
  -scheme "$SCHEME" \
  -destination "$DESTINATION" \
  CODE_SIGNING_ALLOWED=NO \
  | xcpretty || xcodebuild test \
  -project "$PROJECT_PATH" \
  -scheme "$SCHEME" \
  -destination "$DESTINATION" \
  CODE_SIGNING_ALLOWED=NO

echo "✅ iOS tests passed!"
