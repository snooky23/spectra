#!/bin/bash

# Script to add URL scheme to the Xcode project
# This enables deep linking to open Spectra Logger from other apps

set -e

echo "📱 Adding URL scheme to SpectraExample..."

PROJECT_FILE="SpectraExample.xcodeproj/project.pbxproj"

if [ ! -f "$PROJECT_FILE" ]; then
    echo "❌ Error: $PROJECT_FILE not found"
    echo "Make sure you're in the examples/ios-native directory"
    exit 1
fi

# Check if URL scheme is already added
if grep -q "spectralogger" "$PROJECT_FILE"; then
    echo "✅ URL scheme 'spectralogger' is already configured"
    exit 0
fi

echo "⚠️  Manual configuration required:"
echo ""
echo "To add the URL scheme, open Xcode and follow these steps:"
echo ""
echo "1. Open SpectraExample.xcodeproj (or .xcworkspace if using CocoaPods)"
echo "2. Select the SpectraExample target"
echo "3. Go to the 'Info' tab"
echo "4. Expand 'URL Types'"
echo "5. Click the '+' button to add a new URL Type"
echo "6. Configure:"
echo "   - Identifier: com.spectra.logger"
echo "   - URL Schemes: spectralogger"
echo "   - Role: Editor"
echo ""
echo "Or add this to your Info.plist:"
echo ""
cat << 'EOF'
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLName</key>
        <string>com.spectra.logger</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>spectralogger</string>
        </array>
    </dict>
</array>
EOF
echo ""
echo "After adding, you can test with:"
echo "  xcrun simctl openurl booted spectralogger://logs"
echo ""
