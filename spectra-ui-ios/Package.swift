// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

// MARK: - Dependency Configuration
// This package can be used in two ways:
//
// 1. LOCAL DEVELOPMENT (default)
//    Uses local Spectra package from the root directory
//    Relative path: ../ (root Package.swift provides SpectraLogger XCFramework)
//    When: Working on both packages together in the same workspace
//
// 2. RELEASE MODE
//    Uses published Spectra from Swift Package Registry or GitHub
//    When: Using released versions, after packages are published
//
// To switch between modes, modify the dependencies array below

let package = Package(
    name: "spectra-ui-ios",
    platforms: [
        .iOS(.v17),
    ],
    products: [
        .library(
            name: "SpectraUI",
            targets: ["SpectraUI"]
        ),
    ],
    dependencies: [
        // LOCAL DEVELOPMENT MODE (default)
        // Uses the root Package.swift which provides SpectraLogger XCFramework
        .package(path: ".."),

        // RELEASE MODE (uncomment after packages are published)
        // GitHub release:
        // .package(url: "https://github.com/snooky23/spectra.git", from: "1.0.0"),
        //
        // Swift Package Index (future):
        // .package(url: "https://github.com/snooky23/spectra.git", .upToNextMajor(from: "1.0.0")),
    ],
    targets: [
        // SwiftUI views and ViewModels for iOS
        .target(
            name: "SpectraUI",
            dependencies: [
                .product(name: "SpectraLogger", package: "Spectra")
            ],
            path: "Sources/SpectraLoggerUI"
        ),

        .testTarget(
            name: "SpectraUITests",
            dependencies: ["SpectraUI"]
        ),
    ]
)
