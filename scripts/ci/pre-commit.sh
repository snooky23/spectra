#!/bin/bash
set -e

# Pre-commit hook
# Run this before committing to ensure code quality

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🔍 Running pre-commit checks..."

cd "$PROJECT_ROOT"

# 1. Check formatting
echo "📝 Checking Kotlin code formatting..."
./gradlew ktlintCheck || {
    echo "❌ Kotlin formatting issues found. Run './gradlew ktlintFormat' to fix."
    exit 1
}

# 2. Run tests
echo "🧪 Running tests..."
"$SCRIPT_DIR/../test/test-all.sh" || {
    echo "❌ Tests failed. Please fix before committing."
    exit 1
}

# 3. Check build
echo "🔨 Checking builds..."
"$SCRIPT_DIR/../build/build-kmp.sh" || {
    echo "❌ Build failed. Please fix before committing."
    exit 1
}

echo "✅ All pre-commit checks passed!"
