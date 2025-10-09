#!/usr/bin/env python3

import os
import sys

# Install mod-pbxproj if not already installed
try:
    from mod_pbxproj import XcodeProject
except ImportError:
    print("Installing mod-pbxproj...")
    os.system("pip3 install mod-pbxproj")
    from mod_pbxproj import XcodeProject

project = XcodeProject.load('SpectraExample.xcodeproj/project.pbxproj')

# Add ViewModel files
viewmodel_files = [
    'SpectraExample/ViewModels/LogsViewModel.swift',
    'SpectraExample/ViewModels/NetworkLogsViewModel.swift',
    'SpectraExample/ViewModels/SettingsViewModel.swift'
]

# Add View files
view_files = [
    'SpectraExample/Views/LogsView.swift',
    'SpectraExample/Views/NetworkLogsView.swift',
    'SpectraExample/Views/SettingsView.swift'
]

# Add all files to the project
all_files = viewmodel_files + view_files
for file_path in all_files:
    if os.path.exists(file_path):
        project.add_file(file_path, target_name='SpectraExample')
        print(f"Added {file_path}")
    else:
        print(f"WARNING: {file_path} does not exist!")

project.save()
print("Project updated successfully!")
