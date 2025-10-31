// swift-tools-version:5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "SpectraLogger",
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
        // Binary target for XCFramework
        //
        // For local development (building from source):
        //   Uncomment the path-based target below and comment out the URL-based target
        //
        // For distribution (using released binary):
        //   Use the URL-based target (default)

        // LOCAL DEVELOPMENT: Uncomment for local builds
        .binaryTarget(
            name: "SpectraLogger",
            path: "build/xcframework/SpectraLogger.xcframework"
        )

        // DISTRIBUTION: Released binary from GitHub Releases
        // Uncomment for distribution builds
        // .binaryTarget(
        //     name: "SpectraLogger",
        //     url: "https://github.com/snooky23/spectra/releases/download/v1.0.0/SpectraLogger.xcframework.zip",
        //     checksum: "2d1892d4f8836ab59e0a5bb8c4e1de06af1ef96fbae357923126f08cfa8b7bef"
        // )
    ]
)
