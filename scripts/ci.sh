#!/bin/bash

# CI script - Runs the complete CI pipeline locally
# This script replicates the full GitHub Actions workflow

set -e  # Exit on error

echo "🚀 Running Full CI Pipeline..."
echo "================================"

# Run build
echo ""
echo "Step 1/4: Build"
./scripts/build.sh

# Run tests
echo ""
echo "Step 2/4: Tests"
./scripts/test.sh

# Run code quality checks
echo ""
echo "Step 3/4: Code Quality"
./scripts/code-quality.sh

# Generate coverage
echo ""
echo "Step 4/4: Coverage"
./scripts/coverage.sh

echo ""
echo "================================"
echo "✅ Full CI pipeline completed successfully!"
echo ""
