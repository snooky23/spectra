#!/bin/bash
set -e

# Run all tests - KMP, iOS, Android
# Main test script for CI/CD

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🧪 Running All Tests..."
echo "====================="

# 1. KMP tests
echo ""
echo "Step 1/3: Running KMP Tests..."
"$SCRIPT_DIR/test-kmp.sh"

# 2. iOS tests
echo ""
echo "Step 2/3: Running iOS Tests..."
"$SCRIPT_DIR/test-ios.sh"

# 3. Android tests
echo ""
echo "Step 3/3: Running Android Tests..."
"$SCRIPT_DIR/test-android.sh"

echo ""
echo "====================="
echo "✅ All tests passed!"
echo "====================="
