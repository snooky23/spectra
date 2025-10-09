// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

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
    targets: [
        // SwiftUI views and ViewModels for iOS
        .target(
            name: "SpectraLoggerUI",
            dependencies: ["SpectraLogger"],
            path: "Sources/SpectraLoggerUI"
        ),

        // KMP Framework (binary target)
        // Note: Build the XCFramework first by running:
        // cd ../shared && ./gradlew :shared:linkReleaseFrameworkIosArm64 :shared:linkReleaseFrameworkIosSimulatorArm64
        // then create XCFramework manually or use the provided script
        .binaryTarget(
            name: "SpectraLogger",
            path: "../shared/build/XCFrameworks/release/SpectraLogger.xcframework"
        ),

        .testTarget(
            name: "SpectraLoggerUITests",
            dependencies: ["SpectraLoggerUI"]
        ),
    ]
)
