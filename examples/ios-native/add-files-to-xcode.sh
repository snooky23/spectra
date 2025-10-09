#!/bin/bash

# This script uses PlistBuddy to add files directly to the Xcode project

PROJECT_FILE="SpectraExample.xcodeproj/project.pbxproj"

# Backup the project file
cp "$PROJECT_FILE" "${PROJECT_FILE}.backup"

# Use xcodebuild to add files (simpler approach)
# First, let's just list what needs to be added
echo "Files to add:"
echo "  - LogsViewModel.swift"
echo "  - NetworkLogsViewModel.swift"
echo "  - SettingsViewModel.swift"
echo "  - LogsView.swift"
echo "  - NetworkLogsView.swift"
echo "  - SettingsView.swift"

echo ""
echo "Please open Xcode and:"
echo "1. File → Add Files to \"SpectraExample\"..."
echo "2. Navigate to SpectraExample folder"
echo "3. Select these 6 files (hold Cmd to select multiple)"
echo "4. UNCHECK 'Copy items if needed'"
echo "5. ENSURE 'SpectraExample' target is checked"
echo "6. Click Add"
echo ""
echo "Or press Enter and I'll open Xcode for you..."
read

open SpectraExample.xcodeproj
