#!/usr/bin/env swift

import Foundation

// This script creates an Xcode project structure for the iOS example app
// It generates the necessary project.pbxproj file and supporting structure

let projectName = "SpectraExample"
let projectDir = FileManager.default.currentDirectoryPath
let projectPath = "\(projectDir)/\(projectName).xcodeproj"

print("Creating Xcode project structure at: \(projectPath)")

// Create the .xcodeproj bundle structure
try! FileManager.default.createDirectory(atPath: projectPath, withIntermediateDirectories: true)

// Create project.pbxproj - minimal viable structure
let pbxprojContent = """
// !$*UTF8*$!
{
	archiveVersion = 1;
	classes = {
	};
	objectVersion = 55;
	objects = {
		00000000000000000000000000000001 /* PBXGroup */ = {
			isa = PBXGroup;
			children = (
				00000000000000000000000000000002,
				00000000000000000000000000000003,
			);
			sourceTree = SOURCE_ROOT;
		};
		00000000000000000000000000000002 /* SpectraExample */ = {
			isa = PBXGroup;
			children = (
				00000000000000000000000000000004,
				00000000000000000000000000000005,
				00000000000000000000000000000006,
			);
			path = SpectraExample;
			sourceTree = SOURCE_ROOT;
		};
		00000000000000000000000000000003 /* Products */ = {
			isa = PBXGroup;
			children = (
				00000000000000000000000000000010,
			);
			name = Products;
			sourceTree = "<group>";
		};
		00000000000000000000000000000004 /* SpectraExampleApp.swift */ = {
			isa = PBXFileReference;
			lastKnownFileType = sourcecode.swift;
			path = SpectraExampleApp.swift;
			sourceTree = "<group>";
		};
		00000000000000000000000000000005 /* MainAppView.swift */ = {
			isa = PBXFileReference;
			lastKnownFileType = sourcecode.swift;
			path = MainAppView.swift;
			sourceTree = "<group>";
		};
		00000000000000000000000000000006 /* SpectraLoggerView.swift */ = {
			isa = PBXFileReference;
			lastKnownFileType = sourcecode.swift;
			path = SpectraLoggerView.swift;
			sourceTree = "<group>";
		};
		00000000000000000000000000000010 /* SpectraExample.app */ = {
			isa = PBXFileReference;
			explicitFileType = wrapper.application;
			includeInIndex = 0;
			path = SpectraExample.app;
			sourceTree = BUILT_PRODUCTS_DIR;
		};
	};
	rootObject = 00000000000000000000000000000001 /* Project object */;
}
"""

print("Note: Creating a minimal pbxproj structure")
print("This requires Xcode to be used for proper project setup")
print("")
print("Instead, please follow these manual steps:")
print("")
print("1. In Xcode: File → New → Project")
print("2. Select: iOS → App")
print("3. Configure:")
print("   - Product Name: SpectraExample")
print("   - Team: (your team)")
print("   - Organization: Spectra")
print("   - Bundle Identifier: com.spectra.logger.example")
print("   - Language: Swift")
print("   - SwiftUI: Yes")
print("   - Save in: examples/ios-native/")
print("")
print("4. Then add SPM dependencies:")
print("   - Select SpectraExample target")
print("   - Build Phases → Link Binary With Libraries")
print("   - Click + → Add Packages")
print("   - Add Local: ../../SpectraLoggerUI")
print("")
print("This approach ensures Xcode properly generates all required files.")
