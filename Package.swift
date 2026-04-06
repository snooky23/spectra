// swift-tools-version:5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription
import Foundation

// MARK: - Configuration
// Use environment variable to control local vs release mode:
//   SPECTRA_LOCAL_DEV=1 swift build    → Uses local XCFramework
//   swift build                        → Uses GitHub release (default for consumers)
//
// NOTE: For local development, the XCFrameworks must exist at build/xcframework/

// Get the directory containing this Package.swift
let packageDir = URL(fileURLWithPath: #filePath).deletingLastPathComponent().path

let localCorePath = "build/xcframework/SpectraLogger.xcframework"
let localUIPath = "build/xcframework/SpectraLoggerUI.xcframework"

let absoluteCorePath = packageDir + "/" + localCorePath
let absoluteUIPath = packageDir + "/" + localUIPath

let useLocalDev = ProcessInfo.processInfo.environment["SPECTRA_LOCAL_DEV"] != nil
    || (FileManager.default.fileExists(atPath: absoluteCorePath) && FileManager.default.fileExists(atPath: absoluteUIPath))

let package = Package(
    name: "Spectra",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "SpectraLogger",
            targets: ["SpectraLogger"]
        ),
        .library(
            name: "SpectraLoggerUI",
            targets: ["SpectraLoggerUI"]
        )
    ],
    targets: [
        // Core Logic binary target
        useLocalDev
            ? .binaryTarget(
                name: "SpectraLogger",
                path: localCorePath
            )
            : .binaryTarget(
                name: "SpectraLogger",
                url: "https://github.com/snooky23/spectra/releases/download/v1.0.4/SpectraLogger.xcframework.zip",
                checksum: "e935f93b0e9287fe067b0c3c1cc2f5c5ce097362ef3e1c3427bc9567a6ad9295"
            ),
            
        // UI SDK binary target (Compose Multiplatform)
        useLocalDev
            ? .binaryTarget(
                name: "SpectraLoggerUI",
                path: localUIPath
            )
            : .binaryTarget(
                name: "SpectraLoggerUI",
                url: "https://github.com/snooky23/spectra/releases/download/v1.0.4/SpectraLoggerUI.xcframework.zip",
                checksum: "PLACEHOLDER_CHECKSUM" // Will be updated during release
            )
    ]
)
