#!/usr/bin/env python3
import subprocess
import os
import sys

def create_ios_app():
    """Create a new iOS app Xcode project with SPM integration"""
    
    project_name = "SpectraExample"
    team_id = "-"  # Will prompt or use default
    
    # Create the project using Xcode templates
    cmd = [
        "xcode-select",
        "--install"
    ]
    
    # For now, we'll document the manual steps since Xcode project creation
    # via command line is complex
    print("To create the Xcode project:")
    print("1. Open Xcode")
    print("2. File → New → Project")
    print("3. Choose: iOS → App")
    print("4. Configure:")
    print("   - Product Name: SpectraExample")
    print("   - Team: (your team)")
    print("   - Organization: Spectra")
    print("   - Language: Swift")
    print("   - SwiftUI: Yes")
    print("5. Save in: examples/ios-native/")

if __name__ == "__main__":
    create_ios_app()
