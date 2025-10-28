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
    dependencies: [
        // Core logging framework (Kotlin Multiplatform compiled to iOS)
        .package(path: "../SpectraLogger")
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
