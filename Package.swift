// swift-tools-version:5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription
import Foundation

// MARK: - Configuration
// Use environment variable to control local vs release mode:
//   SPECTRA_LOCAL_DEV=1 swift build    → Uses local XCFramework
//   swift build                        → Uses GitHub release (default for consumers)
//
// NOTE: For local development, the XCFramework must exist at build/xcframework/

// Get the directory containing this Package.swift
let packageDir = URL(fileURLWithPath: #filePath).deletingLastPathComponent().path
let localXCFrameworkPath = "build/xcframework/SpectraLogger.xcframework"
let absoluteLocalPath = packageDir + "/" + localXCFrameworkPath

let useLocalDev = ProcessInfo.processInfo.environment["SPECTRA_LOCAL_DEV"] != nil
    || FileManager.default.fileExists(atPath: absoluteLocalPath)

let package = Package(
    name: "Spectra",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "SpectraLogger",
            targets: ["SpectraLogger"]
        )
    ],
    targets: [
        // Dynamic target: local dev if XCFramework exists, otherwise release URL
        useLocalDev
            ? .binaryTarget(
                name: "SpectraLogger",
                path: localXCFrameworkPath
            )
            : .binaryTarget(
                name: "SpectraLogger",
                url: "https://github.com/snooky23/spectra/releases/download/v1.0.1/SpectraLogger.xcframework.zip",
                checksum: "PLACEHOLDER_CHECKSUM"
            )
    ]
)

