// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

// MARK: - Dependency Configuration
// This package can be used in two ways:
//
// 1. LOCAL DEVELOPMENT (default)
//    Uses local SpectraLogger package for debugging
//    Relative path: ../SpectraLogger
//    When: Working on both packages together in the same workspace
//
// 2. RELEASE MODE
//    Uses published SpectraLogger from Swift Package Registry or GitHub
//    When: Using released versions, after packages are published
//
// To switch between modes, modify the dependencies array below

let package = Package(
    name: "SpectraLoggerUI",
    platforms: [
        .iOS(.v15),
    ],
    products: [
        .library(
            name: "SpectraLoggerUI",
            targets: ["SpectraLoggerUI"]
        ),
    ],
    dependencies: [
        // LOCAL DEVELOPMENT MODE (uncomment for local development)
        // This uses the local SpectraLogger package from the same workspace
        .package(path: "../SpectraLogger"),

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
            name: "SpectraLoggerUI",
            dependencies: [
                .product(name: "SpectraLogger", package: "SpectraLogger")
            ],
            path: "Sources/SpectraLoggerUI"
        ),

        .testTarget(
            name: "SpectraLoggerUITests",
            dependencies: ["SpectraLoggerUI"]
        ),
    ]
)
