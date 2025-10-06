#!/bin/bash

# Script to set up CocoaPods for the iOS example app with local SpectraLogger framework

set -e  # Exit on error

echo "🚀 Setting up CocoaPods for iOS example..."

# Check if CocoaPods is installed
if ! command -v pod &> /dev/null; then
    echo "❌ CocoaPods is not installed!"
    echo "Install it with: sudo gem install cocoapods"
    exit 1
fi

echo "✅ CocoaPods is installed"

# Navigate to the ios-native directory
cd "$(dirname "$0")"

echo "📦 Installing CocoaPods dependencies..."
echo "   This will build the Kotlin framework for iOS..."

# Install pods (this will trigger the prepare_command in the podspec)
pod install

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Open SpectraExample.xcworkspace (NOT .xcodeproj)"
echo "   open SpectraExample.xcworkspace"
echo ""
echo "2. Build and run the app in Xcode (Cmd+R)"
echo ""
echo "Note: After making changes to the shared module, run:"
echo "   pod update SpectraLogger"
echo "   to rebuild the framework"
