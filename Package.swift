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

let localUmbrellaPath: String = {
    if FileManager.default.fileExists(atPath: packageDir + "/SpectraFrameworks/Spectra.xcframework") {
        return "SpectraFrameworks/Spectra.xcframework"
    }
    return "build/xcframework/Spectra.xcframework"
}()

let localCorePath: String = {
    if FileManager.default.fileExists(atPath: packageDir + "/SpectraFrameworks/SpectraLogger.xcframework") {
        return "SpectraFrameworks/SpectraLogger.xcframework"
    }
    return "build/xcframework/SpectraLogger.xcframework"
}()

let localUIPath: String = {
    if FileManager.default.fileExists(atPath: packageDir + "/SpectraFrameworks/SpectraLoggerUI.xcframework") {
        return "SpectraFrameworks/SpectraLoggerUI.xcframework"
    }
    return "build/xcframework/SpectraLoggerUI.xcframework"
}()

let absoluteUmbrellaPath = packageDir + "/" + localUmbrellaPath
let absoluteCorePath = packageDir + "/" + localCorePath
let absoluteUIPath = packageDir + "/" + localUIPath

let useLocalDev = ProcessInfo.processInfo.environment["SPECTRA_LOCAL_DEV"] != nil
    || (FileManager.default.fileExists(atPath: absoluteUmbrellaPath)
        || (FileManager.default.fileExists(atPath: absoluteCorePath) && FileManager.default.fileExists(atPath: absoluteUIPath)))

let package = Package(
    name: "Spectra",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        // Unified umbrella framework (Recommended)
        .library(
            name: "Spectra",
            targets: ["Spectra"]
        ),
        // Standalone Core & UI libraries for specialized consumers
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
        // Unified Umbrella binary target (Core + UI)
        useLocalDev
            ? .binaryTarget(
                name: "Spectra",
                path: localUmbrellaPath
            )
            : .binaryTarget(
                name: "Spectra",
                url: "https://github.com/snooky23/spectra/releases/download/v1.0.4/Spectra.xcframework.zip",
                checksum: "e935f93b0e9287fe067b0c3c1cc2f5c5ce097362ef3e1c3427bc9567a6ad9295"
            ),

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
                url: "https://github.com/snooky23/spectra/releases/download/v1.0.4/SpectraLogger.xcframework.zip",
                checksum: "e935f93b0e9287fe067b0c3c1cc2f5c5ce097362ef3e1c3427bc9567a6ad9295"
            )
    ]
)
