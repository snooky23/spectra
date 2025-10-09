#!/bin/bash

# Simple approach: use xed (Xcode editor) command to open and let Xcode handle it
# But first, let's try using xcfilelist approach

# Create a filelist
cat > files_to_add.txt << FILELIST
$(pwd)/SpectraExample/LogsViewModel.swift
$(pwd)/SpectraExample/NetworkLogsViewModel.swift
$(pwd)/SpectraExample/SettingsViewModel.swift
$(pwd)/SpectraExample/LogsView.swift
$(pwd)/SpectraExample/NetworkLogsView.swift
$(pwd)/SpectraExample/SettingsView.swift
FILELIST

echo "Created file list. Opening Xcode..."
echo "Please drag these files from Finder into the Xcode project navigator."
echo ""
cat files_to_add.txt
echo ""

# Open Xcode
open SpectraExample.xcodeproj

# Open Finder to the folder
open SpectraExample/
